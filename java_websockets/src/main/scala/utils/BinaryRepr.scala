package utils

extension (s: StringContext)
  def b(): BinaryRepr = BinaryRepr(s.parts.reduce(_ + _))

/**
 * Binary representation as a string
 * e.g. "1011101"
 *
 * @param binary the binary representation
 */
class BinaryRepr(binary: String) {
  /**
   * Transform this binary representation to a byte
   *
   * @return the byte representation of this
   */
  def toByte: Byte = Integer.parseInt(binary, 2).toByte

  override def toString: String = f"BinaryRepr($binary)"
}
