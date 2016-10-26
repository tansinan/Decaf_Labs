/* This is based on a Parser source originally created by Zhu Fengmin at 2014-11-07 15:56:21
 * Modified by Shang Shu in Oct. 2016
 *
 * Please do NOT modify it except the definition of tokens.
 */

package decaf.frontend;

import decaf.Location;
import decaf.tree.Tree;
import decaf.tree.Tree.*;
import java.util.*;
import decaf.error.MsgError;
import decaf.Driver;

public class Parser extends BaseParser  {
    public int lookahead = -1;
    public SemValue yylval = new SemValue();
    public static final int YYEOF = -1;

    /* tokens */
    public static final int VOID = 257;
    public static final int BOOL = 258;
    public static final int INT = 259;
    public static final int STRING = 260;
    public static final int CLASS = 261;
    public static final int NULL = 262;
    public static final int EXTENDS = 263;
    public static final int THIS = 264;
    public static final int WHILE = 265;
    public static final int FOR = 266;
    public static final int IF = 267;
    public static final int ELSE = 268;
    public static final int RETURN = 269;
    public static final int BREAK = 270;
    public static final int NEW = 271;
    public static final int PRINT = 277;
    public static final int READ_INTEGER = 278;
    public static final int READ_LINE = 279;
    public static final int LITERAL = 280;
    public static final int IDENTIFIER = 281;
    public static final int AND = 282;
    public static final int OR = 283;
    public static final int STATIC = 284;
    public static final int INSTANCEOF = 285;
    public static final int LESS_EQUAL = 288;
    public static final int GREATER_EQUAL = 289;
    public static final int EQUAL = 290;
    public static final int NOT_EQUAL = 291;
    public static final int PCLONE = 292;
    public static final int SWITCH = 293;
    public static final int CASE = 294;
    public static final int DEFAULT = 295;
    public static final int REPEAT = 296;
    public static final int UNTIL = 297;
    public static final int CONTINUE = 298;

    public void error(String error) {
        yyerror(error);
        Driver.getDriver().checkPoint();
    }

    public int yyparse() {
        if (lookahead < 0) {
            lookahead = yylex();
        }
        ProgramParse();
        if (lookahead != 0 && lookahead != -1) {
            error("syntax error");
        }
        return 0;
    }

    public SemValue MatchToken(int expected) {
        SemValue self = yylval;
        if (lookahead == expected) {
            lookahead = yylex(); // get next token
        } else {
            error("syntax error");
        }
        return self;
    }

    private SemValue ProgramParse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = ClassListParse();
        params[0].clist = new ArrayList<ClassDef>();
        if (params[1].clist != null) { params[0].clist.addAll(params[1].clist);
        } tree = new Tree.TopLevel(params[0].clist, params[0].loc);
        return params[0];
    }

    private SemValue ClassListParse() {
        switch (lookahead) {
            case CLASS:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = ClassDefParse();
                params[2] = ClassListParse();
                params[0].clist = new ArrayList<ClassDef>();
                params[0].clist.add(params[1].cdef);
                if (params[2].clist != null) { params[0].clist.addAll(params[2].clist);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue VariableDefParse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = VariableParse();
        params[2] = MatchToken(';');
        params[0].vdef = params[1].vdef;
        return params[0];
    }

    private SemValue VariableParse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = TypeParse();
        params[2] = MatchToken(IDENTIFIER);
        params[0].vdef = new Tree.VarDef(params[2].ident, params[1].type, params[2].loc);
        return params[0];
    }

    private SemValue SimpleTypeParse() {
        switch (lookahead) {
            case INT:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(INT);
                params[0].type = new Tree.TypeIdent(Tree.INT, params[1].loc);
                return params[0];
            }
            case VOID:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(VOID);
                params[0].type = new Tree.TypeIdent(Tree.VOID, params[1].loc);
                return params[0];
            }
            case BOOL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(BOOL);
                params[0].type = new Tree.TypeIdent(Tree.BOOL, params[1].loc);
                return params[0];
            }
            case STRING:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(STRING);
                params[0].type = new Tree.TypeIdent(Tree.STRING, params[1].loc);
                return params[0];
            }
            case CLASS:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = MatchToken(CLASS);
                params[2] = MatchToken(IDENTIFIER);
                params[0].type = new Tree.TypeClass(params[2].ident, params[1].loc);
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue TypeParse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = SimpleTypeParse();
        params[2] = ArrayParse();
        params[0].type = params[1].type;
        for (int i = 0;
             i < params[2].signal;
             ++i) { params[0].type = new Tree.TypeArray(params[0].type, params[1].loc);
        };
        return params[0];
    }

    private SemValue ArrayParse() {
        switch (lookahead) {
            case '[':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken('[');
                params[2] = MatchToken(']');
                params[3] = ArrayParse();
                params[0].signal = 1 + params[3].signal;
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                params[0].signal = 0;
                return params[0];
            }
        }
    }

    private SemValue ClassDefParse() {
        SemValue[] params = new SemValue[7];
        params[0] = new SemValue();
        params[1] = MatchToken(CLASS);
        params[2] = MatchToken(IDENTIFIER);
        params[3] = ExtendsClauseParse();
        params[4] = MatchToken('{');
        params[5] = FieldListParse();
        params[6] = MatchToken('}');
        params[0].cdef = new Tree.ClassDef(params[2].ident, params[3].ident, params[5].flist, params[1].loc);
        return params[0];
    }

    private SemValue ExtendsClauseParse() {
        switch (lookahead) {
            case EXTENDS:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = MatchToken(EXTENDS);
                params[2] = MatchToken(IDENTIFIER);
                params[0].ident = params[2].ident;
                return params[0];
            }
            case '{':
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue FieldListParse() {
        switch (lookahead) {
            case STATIC:
            case INT:
            case VOID:
            case BOOL:
            case STRING:
            case CLASS:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = FieldParse();
                params[2] = FieldListParse();
                params[0].flist = new ArrayList<Tree>();
                if (params[1].vdef != null) {
                    params[0].flist.add(params[1].vdef);
                } else {
                    params[0].flist.add(params[1].fdef);
                }
                if (params[2].flist != null) {
                    params[0].flist.addAll(params[2].flist);
                }
                return params[0];
            }
            case '}':
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                params[0].flist = new ArrayList<Tree>();
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue FieldParse() {
        switch (lookahead) {
            case STATIC:
            {
                SemValue[] params = new SemValue[8];
                params[0] = new SemValue();
                params[1] = MatchToken(STATIC);
                params[2] = TypeParse();
                params[3] = MatchToken(IDENTIFIER);
                params[4] = MatchToken('(');
                params[5] = FormalsParse();
                params[6] = MatchToken(')');
                params[7] = StmtBlockParse();
                params[0].fdef = new MethodDef(true, params[3].ident, params[2].type, params[5].vlist, (Block) params[7].stmt, params[3].loc);
                return params[0];
            }
            case INT:
            case VOID:
            case BOOL:
            case STRING:
            case CLASS:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = TypeParse();
                params[2] = MatchToken(IDENTIFIER);
                params[3] = SubFieldParse();
                if (params[3].vlist != null) { params[0].fdef = new MethodDef(false, params[2].ident, params[1].type, params[3].vlist, (Block) params[3].stmt, params[2].loc);
                } else { params[0].vdef = new Tree.VarDef(params[2].ident, params[1].type, params[2].loc);
                };
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue SubFieldParse() {
        switch (lookahead) {
            case '(':
            {
                SemValue[] params = new SemValue[5];
                params[0] = new SemValue();
                params[1] = MatchToken('(');
                params[2] = FormalsParse();
                params[3] = MatchToken(')');
                params[4] = StmtBlockParse();
                params[0].vlist = params[2].vlist;
                params[0].stmt = params[4].stmt;
                return params[0];
            }
            case ';':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(';');
                // no actions;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue FormalsParse() {
        switch (lookahead) {
            case INT:
            case VOID:
            case BOOL:
            case STRING:
            case CLASS:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = VariableListParse();
                params[0].vlist = params[1].vlist;
                return params[0];
            }
            case ')':
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                params[0].vlist = new ArrayList<VarDef>();
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue VariableListParse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = VariableParse();
        params[2] = SubVariableListParse();
        params[0].vlist = new ArrayList<VarDef>();
        params[0].vlist.add(params[1].vdef);
        if (params[2].vlist != null) { params[0].vlist.addAll(params[2].vlist);
        };
        return params[0];
    }

    private SemValue SubVariableListParse() {
        switch (lookahead) {
            case ',':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken(',');
                params[2] = VariableParse();
                params[3] = SubVariableListParse();
                params[0].vlist = new ArrayList<VarDef>();
                params[0].vlist.add(params[2].vdef);
                if (params[3].vlist != null) { params[0].vlist.addAll(params[3].vlist);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue StmtBlockParse() {
        SemValue[] params = new SemValue[4];
        params[0] = new SemValue();
        params[1] = MatchToken('{');
        params[2] = StmtListParse();
        params[3] = MatchToken('}');
        params[0].stmt = new Block(params[2].slist, params[1].loc);
        return params[0];
    }

    private SemValue StmtListParse() {
        switch (lookahead) {
            case INT:
            case VOID:
            case BOOL:
            case STRING:
            case CLASS:
            case '-':
            case '!':
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            case ';':
            case IF:
            case SWITCH:
            case WHILE:
            case FOR:
            case REPEAT:
            case RETURN:
            case PRINT:
            case BREAK:
            case '{':
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = StmtParse();
                params[2] = StmtListParse();
                params[0].slist.add(params[1].stmt);
                params[0].slist.addAll(params[2].slist);
                return params[0];
            }
            case '}':
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue StmtParse() {
        switch (lookahead) {
            case INT:
            case VOID:
            case BOOL:
            case STRING:
            case CLASS:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = VariableDefParse();
                params[0].stmt = params[1].vdef;
                return params[0];
            }
            case '-':
            case '!':
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            case ';':
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = SimpleStmtParse();
                params[2] = MatchToken(';');
                if (params[1].stmt == null) { params[0].stmt = new Tree.Skip(params[2].loc);
                } else { params[0].stmt = params[1].stmt;
                };
                return params[0];
            }
            case IF:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = IfStmtParse();
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case SWITCH:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = SwitchStmtParse();
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case WHILE:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = WhileStmtParse();
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case FOR:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = ForStmtParse();
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case REPEAT:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = RepeatStmtParse();
                params[2] = MatchToken(';');
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case RETURN:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = ReturnStmtParse();
                params[2] = MatchToken(';');
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case PRINT:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = PrintStmtParse();
                params[2] = MatchToken(';');
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case BREAK:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = BreakStmtParse();
                params[2] = MatchToken(';');
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            case '{':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = StmtBlockParse();
                params[0].stmt = params[1].stmt;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue SimpleStmtParse() {
        switch (lookahead) {
            case '-':
            case '!':
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = ExprParse();
                params[2] = AssignParse();
                if (params[2].expr == null) {
                    params[0].stmt = new Tree.Exec(params[1].expr, params[1].loc);
                } else {
                    params[0].stmt = new Tree.Assign((LValue)(params[1].expr), params[2].expr, params[2].loc);
                };
                return params[0];
            }
            case ';':
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue AssignParse() {
        switch (lookahead) {
            case '=':
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = MatchToken('=');
                params[2] = ExprParse();
                params[0].loc = params[1].loc;
                params[0].expr = params[2].expr;
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Oper2Parse() {
        SemValue[] params = new SemValue[2];
        params[0] = new SemValue();
        params[1] = MatchToken(OR);
        params[0].signal = Tree.OR;
        params[0].loc = params[1].loc;
        return params[0];
    }

    private SemValue Oper3Parse() {
        SemValue[] params = new SemValue[2];
        params[0] = new SemValue();
        params[1] = MatchToken(AND);
        params[0].signal = Tree.AND;
        params[0].loc = params[1].loc;
        return params[0];
    }

    private SemValue Oper4Parse() {
        switch (lookahead) {
            case EQUAL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(EQUAL);
                params[0].signal = Tree.EQ;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case NOT_EQUAL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(NOT_EQUAL);
                params[0].signal = Tree.NE;
                params[0].loc = params[1].loc;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue Oper5Parse() {
        switch (lookahead) {
            case LESS_EQUAL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(LESS_EQUAL);
                params[0].signal = Tree.LE;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case GREATER_EQUAL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(GREATER_EQUAL);
                params[0].signal = Tree.GE;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case '<':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('<');
                params[0].signal = Tree.LT;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case '>':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('>');
                params[0].signal = Tree.GT;
                params[0].loc = params[1].loc;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue Oper6Parse() {
        switch (lookahead) {
            case '+':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('+');
                params[0].signal = Tree.PLUS;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case '-':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('-');
                params[0].signal = Tree.MINUS;
                params[0].loc = params[1].loc;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue Oper7Parse() {
        switch (lookahead) {
            case '*':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('*');
                params[0].signal = Tree.MUL;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case '/':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('/');
                params[0].signal = Tree.DIV;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case '%':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('%');
                params[0].signal = Tree.MOD;
                params[0].loc = params[1].loc;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue Oper8Parse() {
        switch (lookahead) {
            case '-':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('-');
                params[0].signal = Tree.NEG;
                params[0].loc = params[1].loc;
                return params[0];
            }
            case '!':
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken('!');
                params[0].signal = Tree.NOT;
                params[0].loc = params[1].loc;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ExprParse() {
        SemValue[] params = new SemValue[2];
        params[0] = new SemValue();
        params[1] = TernaryParse();
        params[0].expr = params[1].expr;
        return params[0];
    }

    private SemValue TernaryParse() {
        SemValue ret = new SemValue();
        SemValue[] params = new SemValue[3];
        params[0] = Expr1Parse();
        if(lookahead != '?')
        {
            ret.expr = params[0].expr;
            return ret;
        }
        SemValue op = MatchToken('?');
        params[1] = TernaryParse();
        MatchToken(':');
        params[2] = TernaryParse();
        ret.expr = new Tree.Ternary(Tree.TERNARY, params[0].expr, params[1].expr, params[2].expr, op.loc);
        return ret;
    }

    private SemValue Oper1Parse() {
        switch (lookahead) {
            case PCLONE:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(PCLONE);
                params[0].signal = Tree.PCLONE;
                params[0].loc = params[1].loc;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue Expr1Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr2Parse();
        params[2] = ExprT1Parse();
        params[0].expr = params[1].expr;
        if (params[2].svec != null) {
            for (int i = 0; i < params[2].svec.size(); ++i) {
                params[0].expr = new Tree.Binary(params[2].svec.get(i), params[0].expr, params[2].evec.get(i), params[2].lvec.get(i));
            }
        };
        return params[0];
    }

    private SemValue ExprT1Parse() {
        switch (lookahead) {
            case PCLONE:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = Oper1Parse();
                params[2] = Expr2Parse();
                params[3] = ExprT1Parse();
                params[0].svec = new Vector<Integer>();
                params[0].lvec = new Vector<Location>();
                params[0].evec = new Vector<Expr>();
                params[0].svec.add(params[1].signal);
                params[0].lvec.add(params[1].loc);
                params[0].evec.add(params[2].expr);
                if (params[3].svec != null) {
                    params[0].svec.addAll(params[3].svec);
                    params[0].lvec.addAll(params[3].lvec);
                    params[0].evec.addAll(params[3].evec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr2Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr3Parse();
        params[2] = ExprT2Parse();
        params[0].expr = params[1].expr;
        if (params[2].svec != null) {
            for (int i = 0; i < params[2].svec.size(); ++i) {
                params[0].expr = new Tree.Binary(params[2].svec.get(i), params[0].expr, params[2].evec.get(i), params[2].lvec.get(i));
            }
        };
        return params[0];
    }

    private SemValue ExprT2Parse() {
        switch (lookahead) {
            case OR:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = Oper2Parse();
                params[2] = Expr3Parse();
                params[3] = ExprT2Parse();
                params[0].svec = new Vector<Integer>();
                params[0].lvec = new Vector<Location>();
                params[0].evec = new Vector<Expr>();
                params[0].svec.add(params[1].signal);
                params[0].lvec.add(params[1].loc);
                params[0].evec.add(params[2].expr);
                if (params[3].svec != null) {
                    params[0].svec.addAll(params[3].svec);
                    params[0].lvec.addAll(params[3].lvec);
                    params[0].evec.addAll(params[3].evec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr3Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr4Parse();
        params[2] = ExprT3Parse();
        params[0].expr = params[1].expr;
        if (params[2].svec != null) {
            for (int i = 0; i < params[2].svec.size(); ++i) {
                params[0].expr = new Tree.Binary(params[2].svec.get(i), params[0].expr, params[2].evec.get(i), params[2].lvec.get(i));
            }
        };
        return params[0];
    }

    private SemValue ExprT3Parse() {
        switch (lookahead) {
            case AND:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = Oper3Parse();
                params[2] = Expr4Parse();
                params[3] = ExprT3Parse();
                params[0].svec = new Vector<Integer>();
                params[0].lvec = new Vector<Location>();
                params[0].evec = new Vector<Expr>();
                params[0].svec.add(params[1].signal);
                params[0].lvec.add(params[1].loc);
                params[0].evec.add(params[2].expr);
                if (params[3].svec != null) { params[0].svec.addAll(params[3].svec);
                    params[0].lvec.addAll(params[3].lvec);
                    params[0].evec.addAll(params[3].evec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr4Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr5Parse();
        params[2] = ExprT4Parse();
        params[0].expr = params[1].expr;
        if (params[2].svec != null) {
            for (int i = 0; i < params[2].svec.size(); ++i) {
                params[0].expr = new Tree.Binary(params[2].svec.get(i), params[0].expr, params[2].evec.get(i), params[2].lvec.get(i));
            }
        };
        return params[0];
    }

    private SemValue ExprT4Parse() {
        switch (lookahead) {
            case EQUAL:
            case NOT_EQUAL:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = Oper4Parse();
                params[2] = Expr5Parse();
                params[3] = ExprT4Parse();
                params[0].svec = new Vector<Integer>();
                params[0].lvec = new Vector<Location>();
                params[0].evec = new Vector<Expr>();
                params[0].svec.add(params[1].signal);
                params[0].lvec.add(params[1].loc);
                params[0].evec.add(params[2].expr);
                if (params[3].svec != null) { params[0].svec.addAll(params[3].svec);
                    params[0].lvec.addAll(params[3].lvec);
                    params[0].evec.addAll(params[3].evec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr5Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr6Parse();
        params[2] = ExprT5Parse();
        params[0].expr = params[1].expr;
        if (params[2].svec != null) {
            for (int i = 0; i < params[2].svec.size(); ++i) {
                params[0].expr = new Tree.Binary(params[2].svec.get(i), params[0].expr, params[2].evec.get(i), params[2].lvec.get(i));
            }
        };
        return params[0];
    }

    private SemValue ExprT5Parse() {
        switch (lookahead) {
            case LESS_EQUAL:
            case GREATER_EQUAL:
            case '<':
            case '>':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = Oper5Parse();
                params[2] = Expr6Parse();
                params[3] = ExprT5Parse();
                params[0].svec = new Vector<Integer>();
                params[0].lvec = new Vector<Location>();
                params[0].evec = new Vector<Expr>();
                params[0].svec.add(params[1].signal);
                params[0].lvec.add(params[1].loc);
                params[0].evec.add(params[2].expr);
                if (params[3].svec != null) {
                    params[0].svec.addAll(params[3].svec);
                    params[0].lvec.addAll(params[3].lvec);
                    params[0].evec.addAll(params[3].evec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr6Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr7Parse();
        params[2] = ExprT6Parse();
        params[0].expr = params[1].expr;
        if (params[2].svec != null) {
            for (int i = 0; i < params[2].svec.size(); ++i) {
                params[0].expr = new Tree.Binary(params[2].svec.get(i), params[0].expr, params[2].evec.get(i), params[2].lvec.get(i));
            }
        };
        return params[0];
    }

    private SemValue ExprT6Parse() {
        switch (lookahead) {
            case '+':
            case '-':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = Oper6Parse();
                params[2] = Expr7Parse();
                params[3] = ExprT6Parse();
                params[0].svec = new Vector<Integer>();
                params[0].lvec = new Vector<Location>();
                params[0].evec = new Vector<Expr>();
                params[0].svec.add(params[1].signal);
                params[0].lvec.add(params[1].loc);
                params[0].evec.add(params[2].expr);
                if (params[3].svec != null) {
                    params[0].svec.addAll(params[3].svec);
                    params[0].lvec.addAll(params[3].lvec);
                    params[0].evec.addAll(params[3].evec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr7Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr8Parse();
        params[2] = ExprT7Parse();
        params[0].expr = params[1].expr;
        if (params[2].svec != null) {
            for (int i = 0; i < params[2].svec.size(); ++i) {
                params[0].expr = new Tree.Binary(params[2].svec.get(i), params[0].expr, params[2].evec.get(i), params[2].lvec.get(i));
            }
        };
        return params[0];
    }

    private SemValue ExprT7Parse() {
        switch (lookahead) {
            case '*':
            case '/':
            case '%':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = Oper7Parse();
                params[2] = Expr8Parse();
                params[3] = ExprT7Parse();
                params[0].svec = new Vector<Integer>();
                params[0].lvec = new Vector<Location>();
                params[0].evec = new Vector<Expr>();
                params[0].svec.add(params[1].signal);
                params[0].lvec.add(params[1].loc);
                params[0].evec.add(params[2].expr);
                if (params[3].svec != null) {
                    params[0].svec.addAll(params[3].svec);
                    params[0].lvec.addAll(params[3].lvec);
                    params[0].evec.addAll(params[3].evec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr8Parse() {
        switch (lookahead) {
            case '-':
            case '!':
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = Oper8Parse();
                params[2] = Expr8Parse();
                params[0].expr = new Tree.Unary(params[1].signal, params[2].expr, params[1].loc);
                return params[0];
            }
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = Expr9Parse();
                params[0].expr = params[1].expr;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue Expr9Parse() {
        switch (lookahead) {
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = Expr11Parse();
                params[0].expr = params[1].expr;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue Expr11Parse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = Expr12Parse();
        params[2] = ExprT11Parse();
        params[0].expr = params[1].expr;
        params[0].loc = params[1].loc;
        if (params[2].vec != null) { for (SemValue v : params[2].vec) { if (v.expr != null) { params[0].expr = new Tree.Indexed(params[0].expr, v.expr, params[0].loc);
        } else if (v.elist != null) { params[0].expr = new Tree.CallExpr(params[0].expr, v.ident, v.elist, v.loc);
            params[0].loc = v.loc;
        } else { params[0].expr = new Tree.Ident(params[0].expr, v.ident, v.loc);
            params[0].loc = v.loc;
        } } };
        return params[0];
    }

    private SemValue ExprT11Parse() {
        switch (lookahead) {
            case '[':
            {
                SemValue[] params = new SemValue[5];
                params[0] = new SemValue();
                params[1] = MatchToken('[');
                params[2] = ExprParse();
                params[3] = MatchToken(']');
                params[4] = ExprT11Parse();
                SemValue sem = new SemValue();
                sem.expr = params[2].expr;
                params[0].vec = new Vector<SemValue>();
                params[0].vec.add(sem);
                if (params[4].vec != null) { params[0].vec.addAll(params[4].vec);
                };
                return params[0];
            }
            case '.':
            {
                SemValue[] params = new SemValue[5];
                params[0] = new SemValue();
                params[1] = MatchToken('.');
                params[2] = MatchToken(IDENTIFIER);
                params[3] = ExprAfterIdentParse();
                params[4] = ExprT11Parse();
                SemValue sem = new SemValue();
                sem.ident = params[2].ident;
                sem.loc = params[2].loc;
                sem.elist = params[3].elist;
                params[0].vec = new Vector<SemValue>();
                params[0].vec.add(sem);
                if (params[4].vec != null) { params[0].vec.addAll(params[4].vec);
                };
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue Expr12Parse() {
        switch (lookahead) {
            case LITERAL:
            case NULL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = ConstantParse();
                params[0].expr = params[1].expr;
                return params[0];
            }
            case READ_INTEGER:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken(READ_INTEGER);
                params[2] = MatchToken('(');
                params[3] = MatchToken(')');
                params[0].expr = new Tree.ReadIntExpr(params[1].loc);
                return params[0];
            }
            case READ_LINE:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken(READ_LINE);
                params[2] = MatchToken('(');
                params[3] = MatchToken(')');
                params[0].expr = new Tree.ReadLineExpr(params[1].loc);
                return params[0];
            }
            case THIS:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(THIS);
                params[0].expr = new Tree.ThisExpr(params[1].loc);
                return params[0];
            }
            case NEW:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = MatchToken(NEW);
                params[2] = ExprAfterNewParse();
                if (params[2].ident != null) { params[0].expr = new Tree.NewClass(params[2].ident, params[1].loc);
                } else { params[0].expr = new Tree.NewArray(params[2].type, params[2].expr, params[1].loc);
                };
                return params[0];
            }
            case INSTANCEOF:
            {
                SemValue[] params = new SemValue[7];
                params[0] = new SemValue();
                params[1] = MatchToken(INSTANCEOF);
                params[2] = MatchToken('(');
                params[3] = ExprParse();
                params[4] = MatchToken(',');
                params[5] = MatchToken(IDENTIFIER);
                params[6] = MatchToken(')');
                params[0].expr = new Tree.TypeTest(params[3].expr, params[5].ident, params[1].loc);
                return params[0];
            }
            case '(':
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = MatchToken('(');
                params[2] = ExprAfterLBParse();
                params[0].expr = params[2].expr;
                return params[0];
            }
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = MatchToken(IDENTIFIER);
                params[2] = ExprAfterIdentParse();
                if (params[2].elist != null) { params[0].expr = new Tree.CallExpr(null, params[1].ident, params[2].elist, params[1].loc);
                } else { params[0].expr = new Tree.Ident(null, params[1].ident, params[1].loc);
                };
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ExprAfterNewParse() {
        switch (lookahead) {
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken(IDENTIFIER);
                params[2] = MatchToken('(');
                params[3] = MatchToken(')');
                params[0].ident = params[1].ident;
                return params[0];
            }
            case INT:
            case VOID:
            case BOOL:
            case STRING:
            case CLASS:
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = SimpleTypeParse();
                params[2] = MatchToken('[');
                params[3] = ExprAfterSTParse();
                params[0].type = params[1].type;
                for (int i = 0;
                     i < params[3].signal;
                     ++i) { params[0].type = new Tree.TypeArray(params[0].type, params[1].loc);
                } params[0].expr = params[3].expr;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ExprAfterSTParse() {
        switch (lookahead) {
            case ']':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken(']');
                params[2] = MatchToken('[');
                params[3] = ExprAfterSTParse();
                params[0].expr = params[3].expr;
                params[0].signal = 1 + params[3].signal;
                return params[0];
            }
            case '-':
            case '!':
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = ExprParse();
                params[2] = MatchToken(']');
                params[0].expr = params[1].expr;
                params[0].signal = 0;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ExprAfterLBParse() {
        switch (lookahead) {
            case '-':
            case '!':
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = ExprParse();
                params[2] = MatchToken(')');
                params[0].expr = params[1].expr;
                return params[0];
            }
            case CLASS:
            {
                SemValue[] params = new SemValue[5];
                params[0] = new SemValue();
                params[1] = MatchToken(CLASS);
                params[2] = MatchToken(IDENTIFIER);
                params[3] = MatchToken(')');
                params[4] = ExprParse();
                params[0].expr = new Tree.TypeCast(params[2].ident, params[4].expr, params[4].loc);
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ExprAfterIdentParse() {
        switch (lookahead) {
            case '(':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken('(');
                params[2] = ActualsParse();
                params[3] = MatchToken(')');
                params[0].elist = params[2].elist;
                return params[0];
            }
            case '[':
            case '.':
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ConstantParse() {
        switch (lookahead) {
            case LITERAL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(LITERAL);
                params[0].expr = new Tree.Literal(params[1].typeTag, params[1].literal, params[1].loc);
                return params[0];
            }
            case NULL:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = MatchToken(NULL);
                params[0].expr = new Null(params[1].loc);
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ActualsParse() {
        switch (lookahead) {
            case '-':
            case '!':
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = ExprListParse();
                params[0].elist = params[1].elist;
                return params[0];
            }
            case ')':
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                params[0].elist = new ArrayList<Tree.Expr>();
                return params[0];
            }
            default:
                return new SemValue();
        }
    }

    private SemValue ExprListParse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = ExprParse();
        params[2] = SubExprListParse();
        params[0].elist = new ArrayList<Tree.Expr>();
        params[0].elist.add(params[1].expr);
        params[0].elist.addAll(params[2].elist);
        return params[0];
    }

    private SemValue SubExprListParse() {
        switch (lookahead) {
            case ',':
            {
                SemValue[] params = new SemValue[4];
                params[0] = new SemValue();
                params[1] = MatchToken(',');
                params[2] = ExprParse();
                params[3] = SubExprListParse();
                params[0].elist = new ArrayList<Tree.Expr>();
                params[0].elist.add(params[2].expr);
                params[0].elist.addAll(params[3].elist);
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                params[0].elist = new ArrayList<Tree.Expr>();
                return params[0];
            }
        }
    }

    private SemValue RepeatStmtParse() {
        SemValue ret = new SemValue();
        SemValue repeatTag = MatchToken(REPEAT);
        SemValue statement = StmtParse();
        MatchToken(UNTIL);
        MatchToken('(');
        SemValue expr = ExprParse();
        MatchToken(')');
        ret.stmt = new Tree.RepeatLoop(statement.stmt, expr.expr, repeatTag.loc);
        return ret;
    }

    private SemValue WhileStmtParse() {
        SemValue[] params = new SemValue[6];
        params[0] = new SemValue();
        params[1] = MatchToken(WHILE);
        params[2] = MatchToken('(');
        params[3] = ExprParse();
        params[4] = MatchToken(')');
        params[5] = StmtParse();
        params[0].stmt = new Tree.WhileLoop(params[3].expr, params[5].stmt, params[1].loc);
        return params[0];
    }

    private SemValue ForStmtParse() {
        SemValue[] params = new SemValue[10];
        params[0] = new SemValue();
        params[1] = MatchToken(FOR);
        params[2] = MatchToken('(');
        params[3] = SimpleStmtParse();
        params[4] = MatchToken(';');
        params[5] = ExprParse();
        params[6] = MatchToken(';');
        params[7] = SimpleStmtParse();
        params[8] = MatchToken(')');
        params[9] = StmtParse();
        params[0].stmt = new Tree.ForLoop(params[3].stmt, params[5].expr, params[7].stmt, params[9].stmt, params[1].loc);
        return params[0];
    }

    private SemValue BreakStmtParse() {
        SemValue[] params = new SemValue[2];
        params[0] = new SemValue();
        params[1] = MatchToken(BREAK);
        params[0].stmt = new Tree.Break(params[1].loc);
        return params[0];
    }

    private SemValue CaseParse() {
        SemValue ret = new SemValue();
        SemValue caseTag = MatchToken(CASE);
        SemValue constant = ConstantParse();
        MatchToken(':');
        SemValue statementList = StmtListParse();
        ret.loc = caseTag.loc;
        ret.stmt = new Tree.Case(constant.expr, statementList.slist, caseTag.loc);
        return ret;
    }


    private SemValue SwitchStmtParse() {
        SemValue ret = new SemValue();
        SemValue switchTag = MatchToken(SWITCH);
        ret.loc = switchTag.loc;
        MatchToken('(');
        SemValue expr = ExprParse();
        MatchToken(')');
        MatchToken('{');
        SemValue caseList = CaseListParse();
        SemValue defaultBlock = new SemValue();
        if(lookahead == DEFAULT)
            defaultBlock = DefaultParse();
        MatchToken('}');
        ret.stmt = new Tree.Switch(expr.expr, caseList.caselist, defaultBlock.stmt, ret.loc);
        return ret;
    }

    private SemValue CaseListParse() {
        SemValue ret = new SemValue();
        if(lookahead == CASE)
        {
            SemValue[] params = new SemValue[2];
            params[0] = CaseParse();
            params[1] = CaseListParse();
            ret.loc = params[0].loc;
            ret.caselist.add((Case)params[0].stmt);
            ret.caselist.addAll(params[1].caselist);
        }
        return ret;
    }

    private SemValue DefaultParse() {
        SemValue ret = new SemValue();
        SemValue defaultTag = MatchToken(DEFAULT);
        MatchToken(':');
        SemValue statementList = StmtListParse();
        ret.loc = defaultTag.loc;
        ret.stmt = new Tree.Default(statementList.slist, defaultTag.loc);
        return ret;
    }

    private SemValue IfStmtParse() {
        SemValue[] params = new SemValue[7];
        params[0] = new SemValue();
        params[1] = MatchToken(IF);
        params[2] = MatchToken('(');
        params[3] = ExprParse();
        params[4] = MatchToken(')');
        params[5] = StmtParse();
        params[6] = ElseClauseParse();
        params[0].stmt = new Tree.If(params[3].expr, params[5].stmt, params[6].stmt, params[1].loc);
        return params[0];
    }

    private SemValue ElseClauseParse() {
        switch (lookahead) {
            case ELSE:
            {
                SemValue[] params = new SemValue[3];
                params[0] = new SemValue();
                params[1] = MatchToken(ELSE);
                params[2] = StmtParse();
                params[0].stmt = params[2].stmt;
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue ReturnStmtParse() {
        SemValue[] params = new SemValue[3];
        params[0] = new SemValue();
        params[1] = MatchToken(RETURN);
        params[2] = ReturnExprParse();
        params[0].stmt = new Tree.Return(params[2].expr, params[1].loc);
        return params[0];
    }

    private SemValue ReturnExprParse() {
        switch (lookahead) {
            case '-':
            case '!':
            case LITERAL:
            case NULL:
            case READ_INTEGER:
            case READ_LINE:
            case THIS:
            case NEW:
            case INSTANCEOF:
            case '(':
            case IDENTIFIER:
            {
                SemValue[] params = new SemValue[2];
                params[0] = new SemValue();
                params[1] = ExprParse();
                params[0].expr = params[1].expr;
                return params[0];
            }
            default:
            {
                SemValue[] params = new SemValue[1];
                params[0] = new SemValue();
                // no actions;
                return params[0];
            }
        }
    }

    private SemValue PrintStmtParse() {
        SemValue[] params = new SemValue[5];
        params[0] = new SemValue();
        params[1] = MatchToken(PRINT);
        params[2] = MatchToken('(');
        params[3] = ExprListParse();
        params[4] = MatchToken(')');
        params[0].stmt = new Print(params[3].elist, params[1].loc);
        return params[0];
    }
}

/* end of file */
