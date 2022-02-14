# -*- coding: utf-8 -*-
"""
Created on Sat Oct  2 12:19:57 2021

@author: ZANG HAO
"""


########################################
# xor the byte of char
########################################
def xor_byte(byte_to_xor):
    byte_length = len(byte_to_xor)
    # XOR the byte by excluding the first 2 bits
    result = bool(int(byte_to_xor[2:3]))
    for bit_index in range(3, byte_length):
        result = result ^ bool(int(byte_to_xor[bit_index:bit_index + 1]))
    return bin(result)[:2] + '0' * (10 - len(bin(result))) + bin(result)[2:]


########################################
# make the payload array of a string
########################################
def make_payload_array(message_input):
    # initiate the array
    payload_array = []
    # convert the len of message to byte string padded with 0
    payload_array.append(
        bin(len(message_input))[:2] + '0' * (10 - len(bin(len(message_input)))) + bin(len(message_input))[2:])
    arr = bytes(message_input, 'ascii')
    for a_code in arr:
        # convert the ascii code to byte string padded with 0
        char_byte = bin(a_code)[:2] + '0' * (10 - len(bin(a_code))) + bin(a_code)[2:]
        payload_array.append(char_byte)
        payload_array.append(xor_byte(char_byte))
    return payload_array


########################################
# convert the payload array from byte to bit
########################################
def convert_array_to_bit(payload_array):
    bit_array = []
    for oneByte in payload_array:
        bit_array=bit_array+list(oneByte[2:])
    return bit_array

########################################
# make version 2 QR code in 2d list
########################################
def makeVersion2():
    return [[1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 1, 1, 1, 1, 1, 1],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1],
 [1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1],
 [0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2],
 [0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 2],
 [1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 0, 1, 2, 2, 2, 2],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 1, 2, 2, 2, 2],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2]]
########################################
# make version 1 QR code in 2d list
########################################
def makeVersion1():
    return [[1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 0, 1, 1, 1, 1, 1, 1, 1],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 1, 1, 1, 0, 1],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 0, 1, 0, 0, 0, 0, 0, 1],
 [1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1],
 [0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 0, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 0, 0, 0, 0, 0, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
 [1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2]]

########################################
# append 11101100 00010001 to the whiter space
########################################
def append_to_white_space(payload_bit_array):
    range_index = range(int((375 - len(payload_bit_array)) / 8) + 1)
    for i in range_index:
        if i % 2 == 0:
            payload_bit_array=payload_bit_array+[1,1,1,0,1,1,0,0]
        else:
            payload_bit_array=payload_bit_array+[0,0,0,1,0,0,0,1]
    return payload_bit_array


########################################
# zigzag the QR code with 1 and 0 version2
########################################
"""
input: paylaod 2dlists
output: filled QR code 2dlist
method: view the (x,y)and(x-1,y)as one entity to move up and down for 12 times, 
load the value if left cell and right cell has the sum of 4 (all blank cell in QR 2darray has value 2)
"""
"""
input: paylaod 2dlist
output: filled QR code 2dlist
method: view the (x,y)and(x-1,y)as one entity to move up and down for 12 times, 
load the value if left cell and right cell has the sum of 4 (all blank cell in QR 2darray has value 2)
"""


def make_QR_version2(payload_bit_array):
    qr = makeVersion2()
    # append 11101100 00010001 to white space
    payload_bit_array = append_to_white_space(payload_bit_array)
    for zig_col in range(12, 0, -1):
        # if even then move up else down
        if zig_col % 2:
            row_range = range(0, 25, 1)
        else:
            row_range = range(24, -1, -1)
        # skip the col timing patterns
        if zig_col < 4:
            zig_col_left = zig_col * 2 - 2
            zig_col_right = zig_col * 2 - 1
        else:
            zig_col_left = zig_col * 2 - 1
            zig_col_right = zig_col * 2
        for row in row_range:
            if ((qr[row][zig_col_left] + qr[row][zig_col_right]) == 4):
                qr[row][zig_col_right] = int(payload_bit_array.pop(0))
                qr[row][zig_col_left] = int(payload_bit_array.pop(0))
        # convert 2 to 0
        for row in range(5):
            qr[16 + row][15] = 0
    return qr


########################################
# zigzag the QR code with 1 and 0 version1
########################################
def make_QR_version1(payload_bit_array):
    qr = makeVersion1()
    # append 11101100 00010001 to white space
    payload_bit_array = append_to_white_space(payload_bit_array)
    for zig_col in range(10, 0, -1):
        # if even then move up else down
        if zig_col % 2:
            row_range = range(0, 21, 1)
        else:
            row_range = range(20, -1, -1)
        # skip the col timing patterns
        if zig_col < 4:
            zig_col_left = zig_col * 2 - 2
            zig_col_right = zig_col * 2 - 1
        else:
            zig_col_left = zig_col * 2 - 1
            zig_col_right = zig_col * 2
        for row in row_range:
            if (qr[row][zig_col_left] + qr[row][zig_col_right]) == 4:
                qr[row][zig_col_right] = int(payload_bit_array.pop(0))
                qr[row][zig_col_left] = int(payload_bit_array.pop(0))

    return qr


########################################
# inverse the logistic map bits
########################################
def convert_array_to_bit_reverse(payload_array):
    reverse_bit_array = []
    for oneByte in payload_array:
        for i in range(9, 1, -1):
            reverse_bit_array.append(oneByte[i])
    return reverse_bit_array


########################################
# generate logistic map in reversed bits
########################################
def make_logistic_map_in_bit():
    # for simplity use 25*25/8+1=79 for two version
    byte_num = 0.1
    logistic_map = [byte_num]
    for i in range(78):
        byte_num = float((1.0 - logistic_map[i]) * logistic_map[i] * 4.0)
        logistic_map.append(byte_num)
    logistic_map_new = list(map(lambda x: int(x * 255), logistic_map))
    # convert from int to byte
    logistic_map_byte = []
    for byte_int in logistic_map_new:
        # pad with zero
        logistic_map_byte.append(bin(byte_int)[:2] + '0' * (10 - len(bin(byte_int))) + bin(byte_int)[2:])
    logistic_map_bit = convert_array_to_bit_reverse(logistic_map_byte)
    return logistic_map_bit


########################################
# XOR QR with logistic map
########################################
def logistic_XOR_QR(logistic_map_bit, QR):
    # print(len(logistic_map_bit))
    for row in range(len(QR)):
        for col in range(len(QR[0])):
            QR[row][col] = int(bool(QR[row][col]) ^ bool(int(logistic_map_bit.pop(0))))
    return QR


########################################
# XOR QR with logistic map
########################################

def encode_hex(QR_mapped):
    QR_flatten = sum(QR_mapped, [])
    encoded = ''
    while len(QR_flatten) > 32:
        binary_str = ''
        for i in range(32):
            binary_str += str(QR_flatten.pop(0))
        byte_to_int = int(binary_str, 2)
        hex_str = hex(byte_to_int)
        encoded += hex_str
    # handle bit number less tha or equal to 8
    binary_str = ''
    for i in range(len(QR_flatten)):
        binary_str += str(QR_flatten.pop(0))
    byte_to_int = int(binary_str, 2)
    hex_str = hex(byte_to_int)
    encoded += hex_str
    return encoded


def qrcode_encode_v2(data):
    payload_array = make_payload_array(data)
    payload_bit_array = convert_array_to_bit(payload_array)
    QR_V2 = make_QR_version2(payload_bit_array)
    logistic_map = make_logistic_map_in_bit()
    QR_V2_mapped = logistic_XOR_QR(logistic_map, QR_V2)
    endcoded = encode_hex(QR_V2_mapped)
    return endcoded


def qrcode_encode_v1(data):
    payload_array = make_payload_array(data)
    payload_bit_array = convert_array_to_bit(payload_array)
    QR_V1 = make_QR_version1(payload_bit_array)
    logistic_map = make_logistic_map_in_bit()
    QR_V1_mapped = logistic_XOR_QR(logistic_map, QR_V1)
    endcoded = encode_hex(QR_V1_mapped)
    return endcoded


########################################
# Main
########################################
def main():
    qrcode_encode_v2(data='CC Team is awesome!')


if __name__ == '__main__':
    main()
