import org.antlr.symtab.*;
import org.antlr.v4.runtime.tree.TerminalNode;


public class DefScopesAndSymbols extends TypeScriptBaseListener {

	public static final Type VOID_TYPE = new PrimitiveType("void");
	public static final Type NUMBER_TYPE = new PrimitiveType("number");
	public static final Type STRING_TYPE = new PrimitiveType("string");

	protected Scope currentScope;
	protected Scope globals;

	@Override
	public void enterProgram(TypeScriptParser.ProgramContext ctx) {
		globals = currentScope = new GlobalScope(null); //push
		ctx.scope = globals;
	}

	@Override
	public void exitProgram(TypeScriptParser.ProgramContext ctx) {
		popScope();
	}

	@Override
	public void enterClassDeclarations(TypeScriptParser.ClassDeclarationsContext ctx) {
		String className = ctx.name.getText();
		TypeScriptClassSymbol classSymbol = new TypeScriptClassSymbol(className);
		if (ctx.SuperClassName != null) {
			String superClass = ctx.SuperClassName.getText();
			classSymbol.setSuperClass(superClass);
		}
		currentScope.define(classSymbol);
		ctx.scope = classSymbol;
		pushScope(classSymbol);
	}

	@Override
	public void exitClassDeclarations(TypeScriptParser.ClassDeclarationsContext ctx) {
		popScope();
	}

	@Override
	public void enterFunctionSignature(TypeScriptParser.FunctionSignatureContext ctx) {
		String id = ctx.ID().getText();
		TypeScriptMethodSymbol f = new TypeScriptMethodSymbol(id);
		if (ctx.callSignature().type() != null) {
			currentScope.define(f);
			if (ctx.callSignature().type().getText().equals("string")) {
				f.setType(DefScopesAndSymbols.STRING_TYPE);
			} else { //it is a number
				f.setType(DefScopesAndSymbols.NUMBER_TYPE);
			}
			f.setEnclosingScope(currentScope);
			ctx.scope = f;
			pushScope(f);
		} else {
			currentScope.define(f);
			f.setType(DefScopesAndSymbols.VOID_TYPE);
			f.setEnclosingScope(currentScope);
			ctx.scope = f;
			pushScope(f);
		}
	}

	@Override
	public void exitFunctionSignature(TypeScriptParser.FunctionSignatureContext ctx) {
		popScope();
	}


	@Override
	public void enterBlock(TypeScriptParser.BlockContext ctx) {
		LocalScope locals = new LocalScope(currentScope);
		currentScope.nest(locals);
		ctx.scope = locals;
		pushScope(locals);
	}

	@Override
	public void exitBlock(TypeScriptParser.BlockContext ctx) {
		popScope();
	}

	@Override
	public void enterVardecl(TypeScriptParser.VardeclContext ctx){
		TypeScriptVarSymbol v = new TypeScriptVarSymbol(ctx.ID().getText());
		v.setType(getType(ctx.ID(), ctx.type()));
		currentScope.define(v);

	}

	@Override
	public void enterParameter(TypeScriptParser.ParameterContext ctx) {
		TypeScriptVarSymbol v = new TypeScriptVarSymbol(ctx.ID().getText());
		v.setType(getType(ctx.ID(), ctx.type()));
		currentScope.define(v);
	}


	@Override
	public void enterCommonVarDecl(TypeScriptParser.CommonVarDeclContext ctx){
		TypeScriptFieldSymbol v = new TypeScriptFieldSymbol(ctx.ID().getText());
		v.setType(getType(ctx.ID(), ctx.type()));
		currentScope.define(v);
	}


	//Support
	public Scope getGlobalScope() {
		return globals;
	}


	protected Type getType(TerminalNode ID, TypeScriptParser.TypeContext type){
		String typename = type.getText();
		Type typeSym;
		if (typename.equals("void")){
			typeSym = VOID_TYPE;
		} else if (typename.equals("number")){
			typeSym = NUMBER_TYPE;
		} else if (typename.equals("string")){
			typeSym = STRING_TYPE;
		} else {
			Symbol tmpSym = currentScope.resolve(typename);
			typeSym = (Type)tmpSym;
		}
		return typeSym;

	}

	void pushScope(Scope s) {
		currentScope = s;
	}

	void popScope() {
		if (currentScope != null) {
			currentScope = currentScope.getEnclosingScope();
		}
	}

	public class TypeScriptClassSymbol extends ClassSymbol {
		public TypeScriptClassSymbol(String name) {
			super(name);
		}
	}

	public class TypeScriptMethodSymbol extends MethodSymbol {
		public TypeScriptMethodSymbol(String name) {
			super(name);
		}
	}

	public class TypeScriptVarSymbol extends VariableSymbol {
		public TypeScriptVarSymbol(String name) {
			super(name);
		}
	}

	public class TypeScriptFieldSymbol extends FieldSymbol {
		public TypeScriptFieldSymbol(String name) {
			super(name);
		}
	}
}
