((free scan root old relocate-continue oldcr the-cars the-cdrs new-cars new-cdrs car cdr val continue)
(
start
    (assign the-cars (op create-vector) (const 50) (const nothing))
    (assign the-cdrs (op create-vector) (const 50) (const nothing))
    (assign new-cars (op create-vector) (const 50) (const nothing))
    (assign new-cdrs (op create-vector) (const 50) (const nothing))

    (assign free (const 0))

    (assign car (const (quote a)))
    (assign cdr (const nothing))
    (assign continue (label P1))
    (goto (label cons))
P1
    (assign car (const (quote b)))
    (assign cdr (reg val))
    (assign continue (label P2))
    (goto (label cons))
P2
    (assign car (const (quote c)))
    (assign cdr (reg val))
    (assign continue (label P3))
    (goto (label cons))
P3
    (assign root (reg val))
    (assign root (op vector-ref) (reg the-cdrs) (reg root))
    (perform (op print) (reg free))

begin-garbage-collection
    (assign free (const 0))
    (assign scan (const 0))
    (assign old (reg root))
    (assign relocate-continue (label reassign-root))
    (goto (label relocate-old-result-in-new))
reassign-root
    (assign root (reg new))
    (goto (label gc-loop))

gc-loop
    (test (op =) (reg scan) (reg free))
    (branch (label gc-flip))
    (assign old (op vector-ref) (reg new-cars) (reg scan))
    (assign relocate-continue (label update-car))
    (goto (label relocate-old-result-in-new))

update-car
    (perform (op vector-set!) (reg new-cars) (reg scan) (reg new))
    (assign old (op vector-ref) (reg new-cdrs) (reg scan))

    (assign relocate-continue (label update-cdr))
    (goto (label relocate-old-result-in-new))

update-cdr
    (perform (op vector-set!) (reg new-cdrs) (reg scan) (reg new))
    (assign scan (op +) (reg scan) (const 1))
    (goto (label gc-loop))

relocate-old-result-in-new
    (test (op pointer-to-pair?) (reg old))
    (branch (label pair))
    (assign new (reg old))
    (goto (reg relocate-continue))
pair
    (assign oldcr (op vector-ref) (reg the-cars) (reg old))
    (test (op broken-heart?) (reg oldcr))
    (branch (label already-moved))
    (assign new (reg free))
    (assign free (op +) (reg free) (const 1))
    (perform (op vector-set!) (reg new-cars) (reg new) (reg oldcr))
    (assign oldcr (op vector-ref) (reg the-cdrs) (reg old))
    (perform (op vector-set!) (reg new-cdrs) (reg new) (reg oldcr))
    (perform (op vector-set!) (reg the-cars) (reg old) (const broken-heart))
    (perform (op vector-set!) (reg the-cdrs) (reg old) (reg new))
    (goto (reg relocate-continue))

already-moved
    (assign new (op vector-ref) (reg the-cdrs) (reg old))
    (goto (reg relocate-continue))

gc-flip
    (assign temp (reg the-cdrs))
    (assign the-cdrs (reg new-cdrs))
    (assign new-cdrs (reg temp))
    (assign temp (reg the-cars))
    (assign the-cars (reg new-cars))
    (assign new-cars (reg temp))
    (goto (label end))

cons
    (perform (op vector-set!) (reg the-cars) (reg free) (reg car))
    (perform (op vector-set!) (reg the-cdrs) (reg free) (reg cdr))
    (assign val (reg free))
    (assign free (op +) (reg free) (const 1))
    (goto (reg continue))

end
    (perform (op print) (reg free))
))