package websocket

class BinaryRepr(binary: String) {
  def toByte: Byte = Integer.parseInt(binary, 2).toByte

  override def toString: String = f"BinaryRepr($binary)"
}

object BinaryRepr {
  def fromByte(b: Byte): BinaryRepr = {
    val bin = b.toInt.toBinaryString.takeRight(8)
    println(bin)
    BinaryRepr(bin)
  }
}