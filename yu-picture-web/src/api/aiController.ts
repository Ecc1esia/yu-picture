import request from '@/request'

/** createTextToImageTask POST /api/text2image/create_task */
export async function createTextToImageTask(
  body: {
    prompt: string
    size?: string
    n?: number
    style?: string
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseCreateTextToImageTaskResponse_>(
    '/api/text2image/create_task',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: body,
      ...(options || {}),
    },
  )
}

/** getTextToImageTask GET /api/text2image/get_task/{taskId} */
export async function getTextToImageTask(
  taskId: string,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseGetTextToImageTaskResponse_>(
    `/api/text2image/get_task/${taskId}`,
    {
      method: 'GET',
      ...(options || {}),
    },
  )
}

/** createStyleTransferTask POST /api/style-transfer/create_task */
export async function createStyleTransferTask(
  body: {
    pictureId: number
    style: string
  },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseCreateTextToImageTaskResponse_>(
    '/api/style-transfer/create_task',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: body,
      ...(options || {}),
    },
  )
}

/** getStyleTransferTask GET /api/style-transfer/get_task/{taskId} */
export async function getStyleTransferTask(
  taskId: string,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseGetTextToImageTaskResponse_>(
    `/api/style-transfer/get_task/${taskId}`,
    {
      method: 'GET',
      ...(options || {}),
    },
  )
}
