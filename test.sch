(begin
(define counter (lambda (x) (lambda (y) (set! x (+ x y)) x)) )
(define w1 (counter 0))
(define w2 (counter 0))
(w1 5)
(w1 5)
(w2 5)
)