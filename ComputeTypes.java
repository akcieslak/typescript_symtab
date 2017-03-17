import org.antlr.symtab.*;

public class ComputeTypes extends TypeScriptBaseVisitor<Type> {
	protected StringBuilder buf = new StringBuilder();
	protected Scope currentScope;

	@Override
	public Type visitProgram(TypeScriptParser.ProgramContext ctx) {
		currentScope = ctx.scope;
		return visitChildren(ctx);
	}

	@Override
	public Type visitClassDeclarations(TypeScriptParser.ClassDeclarationsContext ctx) {
		currentScope = ctx.scope;
		return visitChildren(ctx);
	}

	@Override
	public Type visitFunctionSignature(TypeScriptParser.FunctionSignatureContext ctx){
		currentScope = ctx.scope;
		return visitChildren(ctx);
	}

	@Override
	public Type visitBlock(TypeScriptParser.BlockContext ctx){
		currentScope = ctx.scope;
		return visitChildren(ctx);
	}


	@Override
	public Type visitAssign(TypeScriptParser.AssignContext ctx){
		Type n = visit(ctx.expr(1));
		visit(ctx.expr(0));
		return n;
	}

	@Override
	public Type visitVariableRef(TypeScriptParser.VariableRefContext ctx){
		String id = ctx.ID().getText();
		Symbol var = currentScope.resolve(id);
		if (var instanceof DefScopesAndSymbols.TypeScriptVarSymbol)
			ctx.etype = ((DefScopesAndSymbols.TypeScriptVarSymbol)var).getType();
		if (var instanceof DefScopesAndSymbols.TypeScriptFieldSymbol)
			ctx.etype = ((DefScopesAndSymbols.TypeScriptFieldSymbol)var).getType();
		buf.append(id + " is " + ctx.etype.getName() + "\n");
		return ctx.etype;
	}

	@Override
	public Type visitIntRef(TypeScriptParser.IntRefContext ctx){
		ctx.etype = DefScopesAndSymbols.NUMBER_TYPE;
		buf.append(ctx.getText() + " is " + ctx.etype + "\n");
		return ctx.etype;
	}

	@Override
	public Type visitStringLiteral(TypeScriptParser.StringLiteralContext ctx){
		ctx.etype = DefScopesAndSymbols.STRING_TYPE;
		buf.append(ctx.getText() + " is " + ctx.etype + "\n");
		return ctx.etype;
	}

	@Override
	public Type visitThisRef(TypeScriptParser.ThisRefContext ctx){
		Scope scope = currentScope;
		while (!(scope instanceof ClassSymbol)){
			scope = scope.getEnclosingScope();
		}
		buf.append("this is " + scope.getName() + "\n");

		ctx.etype = (Type) scope;
		return ctx.etype;
	}

	@Override
	public Type visitFunctionCall(TypeScriptParser.FunctionCallContext ctx) {
		//takes care of parameters
		if ( ctx.expr()!=null ) {
			visit(ctx.expr());
		}
		String id = ctx.ID().getText();
		Symbol var = currentScope.resolve(id);

		if (var instanceof DefScopesAndSymbols.TypeScriptMethodSymbol){
			ctx.etype = (((DefScopesAndSymbols.TypeScriptMethodSymbol)var).getType());
		}
		else {
			ctx.etype = DefScopesAndSymbols.VOID_TYPE;
		}
		buf.append(ctx.getText() + " is " + ctx.etype + "\n");
		return ctx.etype;
	}

	@Override
	public Type visitFieldRef(TypeScriptParser.FieldRefContext ctx){
		Type n = visit(ctx.expr());
		String id = ctx.ID().getText();

		Symbol typeSym = ((Scope)n).resolve(id);
		ctx.etype = ((VariableSymbol)typeSym).getType();

		buf.append(ctx.getText() + " is " + ctx.etype.getName() + "\n");
		return ctx.etype;
	}


	// S U P P O R T
	public String getRefOutput() {
		return buf.toString();
	}
}
