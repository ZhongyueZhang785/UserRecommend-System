# -*- coding: utf-8 -*-
"""
Created on Sun Oct  3 12:06:24 2021

@author: ZANG HAO
"""
import re
import numpy as np
from .qr_decode_service import BinarySquare


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
    # use32*32/8=128
    byte_num = 0.1
    logistic_map = [byte_num]
    for i in range(127):
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
    for row in range(len(QR)):
        for col in range(len(QR[0])):
            QR[row][col] = int(bool(QR[row][col]) ^ bool(int(logistic_map_bit.pop(0))))
    return QR


########################################
# convert hex string to bit string and detect version
########################################
def hex_str_to_bits(hex_str, number_of_bits):
    # convert to binary and pad with 0
    return bin(int(hex_str, 16))[2:].zfill(number_of_bits)


def convert_hex_str_to_bit(hex_str):
    hex = hex_str
    pat = r'^0x[0-9a-f]*'
    bit_string = ''
    # count to detect version
    count = 0
    while (len(hex) > 0):
        text_list = re.findall(pat, hex)
        if (len(text_list[0]) != len(hex)):
            bit_string += hex_str_to_bits(text_list[0][:-1], 32)
            hex = hex[(len(text_list[0]) - 1):]
            count += 1
        elif (count == 13):
            bit_string += hex_str_to_bits(text_list[0], 25)
            hex = []
            version = 1
        else:
            bit_string += hex_str_to_bits(text_list[0], 17)
            hex = []
            version = 2
    return bit_string, version


def convert_hex_str_to_bit32(hex_str):
    hex = hex_str
    pat = r'^0x[0-9a-f]*'
    bit_string = ''
    while (len(hex) > 0):
        text_list = re.findall(pat, hex)
        if (len(text_list[0]) != len(hex)):
            bit_string += hex_str_to_bits(text_list[0][:-1], 32)
            hex = hex[(len(text_list[0]) - 1):]
        else:
            bit_string += hex_str_to_bits(text_list[0], 32)
            hex = []
    return bit_string


########################################
# fill the bits not for data storage with 2 as a boundary
########################################
def make_boundary(qr, version):
    # top left corner
    for row in range(0, 8):
        for col in range(0, 9):
            qr[row][col] = 2

    if (version == 2):
        # row timing patterns
        for col in range(8, 17, 1):
            qr[6][col] = 2

        # col timing patterns
        for row in range(8, 17, 1):
            qr[row][6] = 2

        # fill the blank near the alignment
        for row in range(5):
            qr[16 + row][15] = 2

        # alignment patterns
        for row in range(16, 21):
            for col in range(16, 21):
                qr[row][col] = 2
        # top right corner
        for row in range(0, 8):
            for col in range(0, 8):
                qr[row][col + 17] = 2

        # bottom left corner
        for row in range(0, 8):
            for col in range(0, 9):
                qr[row + 17][col] = 2
    else:
        # row timing patterns
        for col in range(8, 13, 1):
            qr[6][col] = 2

        # col timing patterns
        for row in range(8, 13, 1):
            qr[row][6] = 2
        # top right corner
        for row in range(0, 8):
            for col in range(0, 8):
                qr[row][col + 13] = 2
            # bottom left corner
        for row in range(0, 8):
            for col in range(0, 9):
                qr[row + 13][col] = 2
    return qr


########################################
# scan QR and retreive the info
########################################
def scan_QR(QR_matrix):
    col_count = len(QR_matrix)
    payload_bit_array = []
    for zig_col in range(int(col_count / 2), 0, -1):
        # if even then move up else down
        if (zig_col % 2):
            row_range = range(0, col_count, 1)
        else:
            row_range = range(col_count - 1, -1, -1)
        # skip the col timing patterns
        if (zig_col < 4):
            zig_col_left = zig_col * 2 - 2
            zig_col_right = zig_col * 2 - 1
        else:
            zig_col_left = zig_col * 2 - 1
            zig_col_right = zig_col * 2
        for row in row_range:
            if ((QR_matrix[row][zig_col_left] + QR_matrix[row][zig_col_right]) <= 2):
                payload_bit_array.append(QR_matrix[row][zig_col_right])
                payload_bit_array.append(QR_matrix[row][zig_col_left])
    return payload_bit_array


########################################
# convert the retreived array to text
########################################
def retrieve_one_byte(payload_bit):
    ascii_byte = ''
    for i in range(8):
        ascii_byte = ascii_byte + str(payload_bit.pop(0))
    return ascii_byte


def payload_to_text(payload_bit):
    text = ''
    char_num = int(retrieve_one_byte(payload_bit), 2)
    for i in range(2 * char_num):
        if (i % 2 == 0):
            ascii_byte = retrieve_one_byte(payload_bit)
            text = text + chr(int(ascii_byte, 2))
        else:
            checksum = retrieve_one_byte(payload_bit)
    return text


########################################
# convert 32*32 map
########################################
def decode_32map(hexstr):
    # from encoded hex to bit
    QR_bit_string = convert_hex_str_to_bit32(hexstr)

    # convert to list and reshape to matrix
    QR_bit_to_decode = list(QR_bit_string)
    QR_matrix_array = np.reshape(QR_bit_to_decode, (32, 32))
    QR_matrix_list = QR_matrix_array.tolist()

    # convert string'1'and'0' to int
    for i in range(len(QR_matrix_list)):
        QR_matrix_list[i] = list(map(int, QR_matrix_list[i]))
    # inititate a logistic map
    logistic_map_decode = make_logistic_map_in_bit()
    # use logistic map to decrypte the QR matrix
    QR_matrix_decrypted = logistic_XOR_QR(logistic_map_decode, QR_matrix_list)
    ########################################
    # find matrix and version from the map
    # QR_matrix_found, QR_version=
    # fill with boundary
    ########################################

    binary_square = BinarySquare(data=QR_matrix_decrypted)
    QR_matrix_found, QR_version = binary_square.find_qr_code()

    QR_matrix_boundary = make_boundary(QR_matrix_found, QR_version)
    # retrieve info bit as list from QR
    payload_bit = scan_QR(QR_matrix_boundary)
    text = payload_to_text(payload_bit)
    return text


########################################
# convert 21/25*21/25 map TO DELETE
########################################
def decode_25or21map(hexstr):
    # from encoded hex to bit
    QR_bit_string, QR_version = convert_hex_str_to_bit(hexstr)
    # convert to list and reshape to matrix
    QR_bit_to_decode = list(QR_bit_string)
    if (QR_version == 2):
        QR_matrix_array = np.reshape(QR_bit_to_decode, (25, 25))
        QR_matrix_list = QR_matrix_array.tolist()
    else:
        QR_matrix_array = np.reshape(QR_bit_to_decode, (21, 21))
        QR_matrix_list = QR_matrix_array.tolist()
    # convert string'1'and'0' to int
    for i in range(len(QR_matrix_list)):
        QR_matrix_list[i] = list(map(int, QR_matrix_list[i]))
    # inititate a logistic map
    logistic_map_decode = make_logistic_map_in_bit()
    # use logistic map to decrypte the QR matrix
    QR_matrix_decrypted = logistic_XOR_QR(logistic_map_decode, QR_matrix_list)
    # fill with boundary
    QR_matrix_boundary = make_boundary(QR_matrix_decrypted, QR_version)
    # retrieve info bit as list from QR
    payload_bit = scan_QR(QR_matrix_boundary)
    text = payload_to_text(payload_bit)
    return text


def main():
    # hexstr = '0x66ede8530xb3b981a10xed18e4040xa4a0026c0xd039db570x21976f0d0xed168440xfdce22bf0xd67e47ec0x2171a0600x2a1a95010x875f3f480x78347f130x886ccc430xc90f439a0x331f54900x7bbcbf030x20d731250xc555223e0x15858'
    # print(decode_25or21map(hexstr))
    hexstr = '0xf35eb3ea0x55635f200x706b91c00xfe9ae5540xa7a6f81b0x9e1729e90x4d868a1b0xbef647e40x7defb3dc0x5e9fbfe90x407f37c40x80bed1c50xdbf1df500xbbcbfbf90xfebd48190xf46460f60x246e6f280x669f0e590x3f7f098c0x6491f7a60xf60f06400x2fe887700x8f3309360x3710be1d0x57a75dec0x61d769ea0x382d1b390x3425b50c0x4cd239a0xd8595dcc0xa1f420bd0x1729e6f9'
    print(decode_32map(hexstr))


if __name__ == '__main__':
    main()
