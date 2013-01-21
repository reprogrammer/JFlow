package edu.illinois.jflow.wala.ui.tools.ir;

import java.util.Iterator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSACFG.ExceptionHandlerBasicBlock;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.util.strings.StringStuff;

public class IRLabelProvider extends LabelProvider implements IEntityStyleProvider {

	private final IRView irView;

	public IRLabelProvider(IRView irView) {
		this.irView= irView;
	}

	/**
	 * Taken from com.ibm.wala.viz.PDFViewUtil
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof BasicBlock) {
			BasicBlock bb= (BasicBlock)element;
			IR ir= irView.getIR();

			StringBuffer result= new StringBuffer();

			int start= bb.getFirstInstructionIndex();
			int end= bb.getLastInstructionIndex();
			result.append("BB").append(bb.getNumber());
			if (bb.isEntryBlock()) {
				result.append(" (en)\n");
			} else if (bb.isExitBlock()) {
				result.append(" (ex)\n");
			}
			if (bb instanceof ExceptionHandlerBasicBlock) {
				result.append("<Handler>");
			}
			result.append("\n");
			for (Iterator<SSAPhiInstruction> it= bb.iteratePhis(); it.hasNext();) {
				SSAPhiInstruction phi= it.next();
				if (phi != null) {
					result.append("           " + phi.toString(ir.getSymbolTable())).append("\n");
				}
			}
			if (bb instanceof ExceptionHandlerBasicBlock) {
				ExceptionHandlerBasicBlock ebb= (ExceptionHandlerBasicBlock)bb;
				SSAGetCaughtExceptionInstruction s= ebb.getCatchInstruction();
				if (s != null) {
					result.append("           " + s.toString(ir.getSymbolTable())).append("\n");
				} else {
					result.append("           " + " No catch instruction. Unreachable?\n");
				}
			}
			SSAInstruction[] instructions= ir.getInstructions();
			for (int j= start; j <= end; j++) {
				if (instructions[j] != null) {
					StringBuffer x= new StringBuffer(j + "   " + instructions[j].toString(ir.getSymbolTable()));
					StringStuff.padWithSpaces(x, 35);
					result.append(x);
					result.append(ir.instructionPosition(j));
					result.append("\n");
				}
			}
			for (Iterator<SSAPiInstruction> it= bb.iteratePis(); it.hasNext();) {
				SSAPiInstruction pi= it.next();
				if (pi != null) {
					result.append("           " + pi.toString(ir.getSymbolTable())).append("\n");
				}
			}
			return result.toString();
		}
		if (element instanceof EntityConnectionData) {
			return "\n";
		}
		return "";
	}

	@Override
	public Color getNodeHighlightColor(Object entity) {
		return null;
	}

	@Override
	public Color getBorderColor(Object entity) {
		return null;
	}

	@Override
	public Color getBorderHighlightColor(Object entity) {
		return null;
	}

	@Override
	public int getBorderWidth(Object entity) {
		return 0;
	}

	@Override
	public Color getBackgroundColour(Object entity) {
		return null;
	}

	@Override
	public Color getForegroundColour(Object entity) {
		return null;
	}

	@Override
	public IFigure getTooltip(Object element) {
		if (element instanceof BasicBlock) {
			BasicBlock bb= (BasicBlock)element;
			IR ir= irView.getIR();
			IDocument doc= irView.getDocument();
			IBytecodeMethod method= (IBytecodeMethod)ir.getMethod();

			StringBuffer result= new StringBuffer();

			int start= bb.getFirstInstructionIndex();
			int end= bb.getLastInstructionIndex();
			SSAInstruction[] instructions= ir.getInstructions();
			for (int j= start; j <= end; j++) {
				if (instructions[j] != null) {
					try {
						int bytecodeIndex= method.getBytecodeIndex(j);
						int sourceLineNum= method.getLineNumber(bytecodeIndex);
						int lineNumber= sourceLineNum - 1; //IDocument indexing is 0-based
						try {
							int lineOffset= doc.getLineOffset(lineNumber);
							int lineLength= doc.getLineLength(lineNumber);
							String sourceCode= doc.get(lineOffset, lineLength).trim();
							result.append(sourceCode);
						} catch (BadLocationException e) {
						}
					} catch (InvalidClassFileException e1) {
						return null;
					}
					result.append("\n");
				}
			}
			return new Label(result.toString());
		}
		return null;
	}

	@Override
	public boolean fisheyeNode(Object entity) {
		return false;
	}

}