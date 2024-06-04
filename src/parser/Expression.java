package parser;

public interface Expression {
    <T> T accept(ExpressionVisitor<T> visitor);
}
