package utils

extension (s: StringContext)
  def b(): BinaryRepr = BinaryRepr(s.parts.reduce(_ + _))

class BinaryRepr(binary: String) {
  def toByte: Byte = Integer.parseInt(binary, 2).toByte

  override def toString: String = f"BinaryRepr($binary)"
}
