package codegen_example.syntax;

public class BinopExp implements Exp {
    public final Exp left;
    public final BOP bop;
    public final Exp right;

    public BinopExp(final Exp left,
                    final BOP bop,
                    final Exp right) {
        this.left = left;
        this.bop = bop;
        this.right = right;
    }

    @Override
    public String toString() {
        return ("(" +
                left.toString() +
                " " +
                bop.toString() +
                " " +
                right.toString() +
                ")");
    }
} // BinopExp
