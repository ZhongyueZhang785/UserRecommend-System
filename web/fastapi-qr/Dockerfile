FROM python:3.7
WORKDIR /web
COPY requirements.txt /web/requirements.txt
RUN pip install --no-cache-dir --upgrade -r /web/requirements.txt
COPY app /web/app
WORKDIR /web/app
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "80"]

