(defun print (int)
    (format-integer (standard-output) int 10)
    (format-char (standard-output) #\newline))

(defglobal foo 1)
(print foo)
(let ((foo 2))
    (print foo))
(print foo)
(setq foo 3)
(print foo)