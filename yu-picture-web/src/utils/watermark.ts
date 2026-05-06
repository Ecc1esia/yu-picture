/**
 * 前端隐形水印工具（LSB 隐写术）
 * 注意：必须使用 PNG 格式保存，JPEG 有损压缩会破坏水印数据
 */

const HEADER = 'YUPIC_WM' // 8 字节标识头

/** 将字符串编码为二进制字符串 */
function textToBinary(text: string): string {
  const encoder = new TextEncoder()
  const bytes = encoder.encode(text)
  let binary = ''
  // 先写入长度（32位整数），便于提取
  const lengthBytes = new Uint8Array(4)
  new DataView(lengthBytes.buffer).setUint32(0, bytes.length, false) // 大端序
  for (const b of lengthBytes) {
    binary += b.toString(2).padStart(8, '0')
  }
  for (const b of bytes) {
    binary += b.toString(2).padStart(8, '0')
  }
  return binary
}

/** 将二进制字符串解码为字符串 */
function binaryToText(binary: string): string {
  if (binary.length < 32) return ''
  const lengthBits = binary.substring(0, 32)
  const length = parseInt(lengthBits, 2)
  if (length <= 0 || length > 10000) return ''

  const bytes: number[] = []
  for (let i = 0; i < length; i++) {
    const start = 32 + i * 8
    if (start + 8 > binary.length) break
    const byteStr = binary.substring(start, start + 8)
    bytes.push(parseInt(byteStr, 2))
  }
  const decoder = new TextDecoder()
  return decoder.decode(new Uint8Array(bytes))
}

/**
 * 将水印嵌入图片
 * @param imageSource 图片 URL 或 HTMLImageElement
 * @param watermarkText 水印文本
 * @returns 包含水印的 PNG Blob
 */
export async function embedWatermark(
  imageSource: string | HTMLImageElement,
  watermarkText: string,
): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = img.width
      canvas.height = img.height
      const ctx = canvas.getContext('2d')!
      ctx.drawImage(img, 0, 0)

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const data = imageData.data

      // 编码水印数据：HEADER + payload
      const payload = HEADER + watermarkText
      const binary = textToBinary(payload)

      // 嵌入 LSB
      let idx = 0
      for (let i = 0; i < data.length && idx < binary.length; i += 4) {
        for (let j = 0; j < 3 && idx < binary.length; j++) {
          // 将当前位的最低有效位替换为水印位
          data[i + j] = (data[i + j] & 0xFE) | parseInt(binary[idx], 2)
          idx++
        }
      }

      ctx.putImageData(imageData, 0, 0)
      canvas.toBlob(
        (blob) => {
          if (blob) resolve(blob)
          else reject(new Error('Canvas 转换失败'))
        },
        'image/png', // 必须使用 PNG 无损格式
      )
    }
    img.onerror = () => reject(new Error('图片加载失败'))
    if (typeof imageSource === 'string') {
      img.src = imageSource
    } else {
      img.src = imageSource.src
    }
  })
}

/**
 * 从图片中提取水印
 * @param imageSource 图片 URL 或 HTMLImageElement
 * @returns 提取到的水印文本
 */
export async function extractWatermark(
  imageSource: string | HTMLImageElement,
): Promise<string> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = img.width
      canvas.height = img.height
      const ctx = canvas.getContext('2d')!
      ctx.drawImage(img, 0, 0)

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const data = imageData.data

      // 从 LSB 提取二进制数据
      let binaryString = ''
      // 最多读取 5000 字节的数据（header + length + content）
      const maxBits = (HEADER.length + 4 + 10000) * 8
      for (let i = 0; i < data.length && binaryString.length < maxBits; i += 4) {
        for (let j = 0; j < 3 && binaryString.length < maxBits; j++) {
          binaryString += (data[i + j] & 0x01).toString()
        }
      }

      // 解码
      const fullText = binaryToText(binaryString)

      // 验证 HEADER
      if (fullText.startsWith(HEADER)) {
        resolve(fullText.substring(HEADER.length))
      } else {
        reject(new Error('未检测到有效水印'))
      }
    }
    img.onerror = () => reject(new Error('图片加载失败'))
    if (typeof imageSource === 'string') {
      img.src = imageSource
    } else {
      img.src = imageSource.src
    }
  })
}
