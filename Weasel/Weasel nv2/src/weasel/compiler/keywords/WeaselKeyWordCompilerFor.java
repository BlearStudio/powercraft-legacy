package weasel.compiler.keywords;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import weasel.compiler.WeaselBlockInfo;
import weasel.compiler.WeaselCompiler;
import weasel.compiler.WeaselCompilerException;
import weasel.compiler.WeaselCompilerReturn;
import weasel.compiler.WeaselKeyWordCompilerHelper;
import weasel.compiler.WeaselToken;
import weasel.compiler.WeaselTokenType;
import weasel.compiler.v2.tokentree.WeaselTree;
import weasel.interpreter.WeaselGenericClass;
import weasel.interpreter.bytecode.WeaselInstruction;
import weasel.interpreter.bytecode.WeaselInstructionIf;
import weasel.interpreter.bytecode.WeaselInstructionJump;
import weasel.interpreter.bytecode.WeaselInstructionJumperDummy;
import weasel.interpreter.bytecode.WeaselInstructionPop;
import weasel.interpreter.bytecode.WeaselInstructionPops;
import weasel.interpreter.bytecode.WeaselInstructionReservate;

public class WeaselKeyWordCompilerFor extends WeaselKeyWordCompiler {

	@Override
	public WeaselCompilerReturn compile(WeaselToken token, WeaselCompiler compiler, WeaselKeyWordCompilerHelper compilerHelpher, ListIterator<WeaselToken> iterator) throws WeaselCompilerException {
		expect(iterator.next(), WeaselTokenType.OPENBRACKET);
		compilerHelpher.openBlock(true);
		List<WeaselInstruction> instructions = WeaselTree.parseAndCompileWhithVarDec(compiler, compilerHelpher, iterator);
		WeaselTree tree2 = WeaselTree.parse(iterator, WeaselTokenType.SEMICOLON);
		if(tree2==null){
			throw new WeaselCompilerException(token.line, "Expect boolean value in secound part of for");
		}
		WeaselTree tree3 = WeaselTree.parse(iterator, WeaselTokenType.CLOSEBRACKET);
		
		WeaselToken t = iterator.next();
		WeaselCompilerReturn wcr;
		if(instructions == null){
			instructions = new ArrayList<WeaselInstruction>();
			instructions.add(new WeaselInstructionJumperDummy());
		}
		WeaselInstruction startJump = instructions.get(instructions.size()-1);
		wcr = tree2.compile(compiler, compilerHelpher, null, new WeaselGenericClass(compiler.baseTypes.voidClass), null, false);
		instructions.addAll(wcr.instructions);
		WeaselTree.autoCast(compiler, wcr.returnType, new WeaselGenericClass(compiler.baseTypes.booleanClass), token.line, instructions, true);
		WeaselInstructionIf ifI;
		instructions.add(ifI = new WeaselInstructionIf());
		if(t.tokenType==WeaselTokenType.OPENBLOCK){
			t = iterator.next();
			while(t.tokenType!=WeaselTokenType.CLOSEBLOCK){
				iterator.previous();
				instructions.addAll(WeaselTree.parseAndCompile(compiler, compilerHelpher, iterator));
				t = iterator.next();
			}
		}else{
			iterator.previous();
			instructions.addAll(WeaselTree.parseAndCompile(compiler, compilerHelpher, iterator));
		}
		WeaselInstruction continueJump = instructions.get(instructions.size()-1);
		if(tree3!=null){
			wcr = tree3.compile(compiler, compilerHelpher, null, new WeaselGenericClass(compiler.baseTypes.voidClass), null, false);
			instructions = wcr.instructions;
			if(wcr.returnType.getBaseClass()!=compiler.baseTypes.voidClass)
				instructions.add(new WeaselInstructionPop());
		}
		WeaselInstruction ifJump = new WeaselInstructionJump(startJump);
		instructions.add(ifJump);
		ifI.setTarget(ifJump);
		WeaselBlockInfo wbi = compilerHelpher.closeBlock();
		int pops = wbi.varsToPop();
		if(pops==1){
			instructions.add(new WeaselInstructionPop());
		}else if(pops>1){
			instructions.add(new WeaselInstructionPops(pops));
		}
		if(pops>=1){
			instructions.add(0, new WeaselInstructionReservate(pops));
		}
		WeaselInstruction breakJump = instructions.get(instructions.size()-1);
		for(WeaselInstructionJump breakI:wbi.breaks){
			breakI.setTarget(breakJump);
		}
		for(WeaselInstructionJump continueI:wbi.continues){
			continueI.setTarget(continueJump);
		}
		return new WeaselCompilerReturn(instructions, new WeaselGenericClass(compiler.baseTypes.voidClass));
	}

}
