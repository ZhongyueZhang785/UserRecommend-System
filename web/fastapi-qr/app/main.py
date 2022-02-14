from typing import Optional
from fastapi import FastAPI
from fastapi.responses import PlainTextResponse
import uvicorn
from service.QR_Encode import qrcode_encode_v1, qrcode_encode_v2
from service.QR_Decode import decode_32map

app = FastAPI()


@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.get("/qrcode", response_class=PlainTextResponse)
def qrcode(type: str, data: str):
    if type == "encode":
        data_len = len(data)
        if data_len > 13:
            return qrcode_encode_v2(data)
        else:
            return qrcode_encode_v1(data)
    elif type == 'decode':
        return decode_32map(data)
    else:
        return 'illegal type'


if __name__ == '__main__':
    uvicorn.run(app, host='0.0.0.0', port=80)
