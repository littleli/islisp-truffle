(requires "testing.lisp")

(let ((stream (open-input-file "../tests/nonportable/file.txt" 2))
      (position nil)
      (datum nil))
  (setf position (file-position stream))
  (test-equal t (integerp position))
  (setf datum (read stream))
  (test-equal '(1 2 3) datum)
  (test-equal nil (= position (file-position stream)))
  (set-file-position stream position)
  (setf datum (read stream))
  (test-equal '(1 2 3) datum)
  (setf datum (read stream))
  (test-equal 'a datum)
  )