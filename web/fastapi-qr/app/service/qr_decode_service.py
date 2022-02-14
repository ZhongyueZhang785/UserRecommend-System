"""
Created on Sat Oct  3 22:00:03 2021

@author: TIAN JIAHE
"""

import math
import numpy as np
from .Rotate import drawMatrix


class BinarySquare:
    data = dict()
    size_x = 0
    size_y = 0

    def __init__(self, data=None, size_x=None, size_y=None, size=None):
        if data:
            if type(data) is dict:
                self.data = data
                self.size_x = max([item[0] for item in data.keys()])
                self.size_y = max([item[1] for item in data.keys()])
            elif type(data) is list:
                self.import_two_dimension(data)
        else:
            if size_x and size_y:
                self.init_empty_data(size_x=size_x, size_y=size_y)
            elif size:
                self.init_empty_data(size_x=size, size_y=size)
            else:
                self.init_empty_data()

    def init_empty_data(self, size_x=32, size_y=32):
        self.import_two_dimension([[0 for j in range(size_y)] for i in range(size_x)])

    def init_data_v1_with_random(self):
        self.init_random_data()
        x = np.random.randint(3, 15)
        y = np.random.randint(3, 15)
        wight_area = self.qr_v1_all_area((x, y))
        self.shade(shade_obj=wight_area, shade_item=0)
        shaded_area = self.qr_v1_area((x, y))
        self.shade(shade_obj=shaded_area)
        self.rotate(clockwise_angle=np.random.choice([0, 90, 180, 270]))

    def init_data_v2_with_random(self):
        self.init_random_data()
        x = np.random.randint(3, 11)
        y = np.random.randint(3, 11)
        wight_area = self.qr_v2_all_area((x, y))
        self.shade(shade_obj=wight_area, shade_item=0)
        shaded_area = self.qr_v2_area((x, y))
        self.shade(shade_obj=shaded_area)
        self.rotate(clockwise_angle=np.random.choice([0, 90, 180, 270]))

    def init_random_data(self, size_x=32, size_y=32):
        result = np.random.randint(2, size=(size_x, size_y)).tolist()
        self.import_two_dimension(result)

    # import with 2d list into the object
    def import_two_dimension(self, two_d_list):
        self.size_x = len(two_d_list)
        self.size_y = len(two_d_list[0])
        for xi, x in enumerate(two_d_list):
            for yi, value in enumerate(x):
                self.data[(xi, yi)] = value

    # export the data into 2d list
    def to_two_dimension_list(self):
        two_d_list = []
        for x in range(self.size_x):
            row = []
            for y in range(self.size_y):
                row.append(self.data.get((x, y)))
            two_d_list.append(row)
        return two_d_list

    # input the position_detection_center, return the all shaded positions in a list
    def position_detection_area(self, position_detection_center):
        shaded_area = []
        wight_area = []
        x = position_detection_center[0]
        y = position_detection_center[1]

        # Add center shaded area
        for xi in range(x - 1, x + 2):
            for yi in range(y - 1, y + 2):
                shaded_area.append((xi, yi))

        # Add outer shaded area
        for xi in range(x - 3, x + 4):
            # left
            shaded_area.append((xi, y - 3))
            # right
            shaded_area.append((xi, y + 3))
        for yi in range(y - 3, y + 4):
            # uppper
            shaded_area.append((x - 3, yi))
            # lowwer
            shaded_area.append((x + 3, yi))

        # Add wight area
        for xi in range(x - 2, x + 3):
            # left
            wight_area.append((xi, y - 2))
            # right
            wight_area.append((xi, y + 2))
        for yi in range(y - 2, y + 3):
            # uppper
            wight_area.append((x - 2, yi))
            # lowwer
            wight_area.append((x + 2, yi))
        return list(set(shaded_area)), list(set(wight_area))

    # input the alignment_center, return the all shaded positions in a list
    def alignment_detection_area(self, alignment_center):
        shaded_area = []
        wight_area = []
        x = alignment_center[0]
        y = alignment_center[1]

        # Add center point
        shaded_area.append((x, y))

        # Add outer shaded area
        for xi in range(x - 2, x + 3):
            # left
            shaded_area.append((xi, y - 2))
            # right
            shaded_area.append((xi, y + 2))
        for yi in range(y - 2, y + 3):
            # uppper
            shaded_area.append((x - 2, yi))
            # lowwer
            shaded_area.append((x + 2, yi))

        # Add wight area
        for xi in range(x - 1, x + 2):
            # left
            shaded_area.append((xi, y - 1))
            # right
            shaded_area.append((xi, y + 1))
        for yi in range(y - 1, y + 2):
            # uppper
            shaded_area.append((x - 1, yi))
            # lowwer
            shaded_area.append((x + 1, yi))
        return list(set(shaded_area)), list(set(wight_area))

    # check whether a point is a position detection center
    def is_position_detection_center(self, point):
        shade_check_positions, wight_check_positions = self.position_detection_area(point)
        shaded_count = 0
        for pos in shade_check_positions:
            if self.data.get(pos) == 1:
                shaded_count += 1
        if shaded_count == 33:
            wight_count = 0
            for pos_wight in wight_check_positions:
                if self.data.get(pos_wight) == 0:
                    wight_count += 1
            if wight_count == 16:
                return True
        return False

    # check whether a point is a alignment detection center
    def is_alignment_detection_center(self, point):
        shade_check_positions, wight_check_positions = self.alignment_detection_area(point)
        shaded_count = 0
        for pos in shade_check_positions:
            if self.data.get(pos) == 1:
                shaded_count += 1
        if shaded_count == 21:
            wight_count = 0
            for pos_wight in wight_check_positions:
                if self.data.get(pos_wight) == 0:
                    wight_count += 1
            if wight_count == 8:
                return True
        return False

    # input the upper left position detection center of a v1 qr code, return the all shaded positions in a list
    def qr_v1_area(self, upper_left_position_detection_center):
        x = upper_left_position_detection_center[0]
        y = upper_left_position_detection_center[1]
        position_detection_centers = [upper_left_position_detection_center, (x, y + 14), (x + 14, y)]
        total_shaded_area = []
        for position_detection_center in position_detection_centers:
            shaded_area, wight_area = self.position_detection_area(position_detection_center)
            total_shaded_area.extend(shaded_area)
        return list(set(total_shaded_area))

    # input the upper left position detection center of a v1 qr code, return all positions in the area
    def qr_v1_all_area(self, upper_left_position_detection_center):
        x = upper_left_position_detection_center[0]
        y = upper_left_position_detection_center[1]
        total_shaded_area = []
        for xi in range(x - 3, x + 18):
            for yi in range(y - 3, y + 18):
                total_shaded_area.append((xi, yi))
        return list(set(total_shaded_area))

    # input the upper left position detection center of a v2 qr code, return the all shaded positions in a list
    def qr_v2_area(self, upper_left_position_detection_center):
        # TODO alignment generation error
        x = upper_left_position_detection_center[0]
        y = upper_left_position_detection_center[1]
        position_detection_centers = [upper_left_position_detection_center, (x, y + 18), (x + 18, y)]
        alignment_detection_centers = [(x + 15, y + 15)]
        total_shaded_area = []
        for position_detection_center in position_detection_centers:
            shaded_area, wight_area = self.position_detection_area(position_detection_center)
            total_shaded_area.extend(shaded_area)
        for alignment_detection_center in alignment_detection_centers:
            shaded_area, wight_area = self.alignment_detection_area(alignment_detection_center)
            total_shaded_area.extend(shaded_area)
        return list(set(total_shaded_area))

    # input the upper left position detection center of a v2 qr code, return all positions in the area
    def qr_v2_all_area(self, upper_left_position_detection_center):
        x = upper_left_position_detection_center[0]
        y = upper_left_position_detection_center[1]
        total_shaded_area = []
        for xi in range(x - 3, x + 22):
            for yi in range(y - 3, y + 22):
                total_shaded_area.append((xi, yi))
        return total_shaded_area

    # shade the corresponding positions
    def shade(self, shade_obj, shade_item=1):
        if type(shade_obj) is list:
            for single_shade_obj in shade_obj:
                if type(single_shade_obj) is tuple:
                    self.data[single_shade_obj] = shade_item
                else:
                    print('single_shade_obj not supported: single_shade_obj={}'.format(single_shade_obj))
        elif type(shade_obj) is tuple:
            self.data[shade_obj] = shade_item
        else:
            print('shade input not supported: shade_obj={}, shade_item={}'.format(shade_obj, shade_item))

    # rotate the square
    def rotate(self, clockwise_angle=90):
        if clockwise_angle != 0:
            two_d_list = self.to_two_dimension_list()
            arr = np.array(two_d_list)
            if clockwise_angle / 90 == 1:
                arr = np.rot90(arr, k=-1)
            elif clockwise_angle / 90 == 2:
                arr = np.rot90(arr, k=2)
            elif clockwise_angle / 90 == 3:
                arr = np.rot90(arr, k=1)
            self.import_two_dimension(arr.tolist())
        else:
            print('No rotation')

    def get_rotate_k(self, success_dict: dict):
        if not success_dict.get('ul'):
            return 2
        if not success_dict.get('ur'):
            return -1
        if not success_dict.get('ll'):
            return 1
        if not success_dict.get('lr'):
            return 0

    # find the QR code in the binary square, and extract the QR code. After extraction, rotate the QR code and
    # export the rotated QR code two dimension list with the version
    # Procedures: 1. looking for position detection center x in [3,14], y in [3,14] (upper left corner)
    #             2. if nothing found, rotate 180 degree clockwise
    #             3. search again for position detection center in the same upper left corner
    def find_qr_code(self):
        for trial in range(2):
            for x in range(3, 15):
                for y in range(3, 15):
                    upper_left_value = self.data.get((x, y))
                    if upper_left_value == 1:
                        # version 1
                        if x + 14 <= 31 and y + 14 <= 31:
                            success_count_1 = 0
                            upper_right_value_1 = self.data.get((x, y + 14), 0)
                            lower_right_value_1 = self.data.get((x + 14, y + 14), 0)
                            lower_left_value_1 = self.data.get((x + 14, y), 0)
                            success_dict = dict()
                            if upper_right_value_1 + lower_right_value_1 + lower_left_value_1 >= 2:
                                if self.is_position_detection_center((x, y)):
                                    success_count_1 += 1
                                    success_dict['ul'] = True
                                if self.is_position_detection_center((x, y + 14)):
                                    success_count_1 += 1
                                    success_dict['ur'] = True
                                if success_count_1 >= 1:
                                    if self.is_position_detection_center((x + 14, y)):
                                        success_count_1 += 1
                                        success_dict['ll'] = True
                                    if success_count_1 >= 2:
                                        if self.is_position_detection_center((x + 14, y + 14)):
                                            success_count_1 += 1
                                            success_dict['lr'] = True
                                            if success_count_1 >= 3:
                                                print('Found QR code!')
                                                k = self.get_rotate_k(success_dict)
                                                arr = np.array(self.to_two_dimension_list())
                                                result = np.rot90(arr[x - 3:x + 18, y - 3:y + 18], k=k).tolist()
                                                version = 1
                                                return result, version
                        # version 2
                        if x + 18 <= 31 and y + 18 <= 31:
                            success_count_2 = 0
                            upper_right_value_2 = self.data.get((x, y + 18), 0)
                            lower_right_value_2 = self.data.get((x + 18, y + 18), 0)
                            lower_left_value_2 = self.data.get((x + 18, y), 0)
                            success_dict = dict()
                            if upper_right_value_2 + lower_right_value_2 + lower_left_value_2 >= 2:
                                if self.is_position_detection_center((x, y)):
                                    success_count_2 += 1
                                    success_dict['ul'] = True
                                if self.is_position_detection_center((x, y + 18)):
                                    success_count_2 += 1
                                    success_dict['ur'] = True
                                if success_count_2 >= 1:
                                    if self.is_position_detection_center((x + 18, y)):
                                        success_count_2 += 1
                                        success_dict['ll'] = True
                                    if success_count_2 >= 2:
                                        if self.is_position_detection_center((x + 18, y + 18)):
                                            success_count_2 += 1
                                            success_dict['lr'] = True
                                            if success_count_2 >= 3:
                                                print('Found QR code!')
                                                k = self.get_rotate_k(success_dict)
                                                arr = np.array(self.to_two_dimension_list())
                                                result = np.rot90(arr[x - 3:x + 22, y - 3:y + 22], k=k).tolist()
                                                version = 2
                                                return result, version
            self.rotate()
        return [[]], 0


class QRCode(BinarySquare):

    def __init__(self, version: int, data=None):
        if data and type(data) is dict:
            super.__init__(data=data)
            # self.data = data
            self.size_x = int(math.sqrt(len(data)))
            self.size_y = int(math.sqrt(len(data)))
        elif data and type(data) is list:
            super.__init__(data=data)
            # self.import_two_dimension(data)
        else:
            if version == 1:
                self.init_data_v1()
            elif version == 2:
                self.init_data_v2()
            else:
                print('version not supported')

    # initialize the QR code with version 1
    def init_data_v1(self):
        self.size_x = 21
        self.size_y = 21
        self.import_two_dimension([[0 for j in range(21)] for i in range(21)])
        position_detection_centers = [(3, 3), (3, 17), (17, 3)]
        total_shaded_area = []
        for position_detection_center in position_detection_centers:
            shaded_area, wight_area = self.position_detection_area(position_detection_center)
            total_shaded_area.extend(shaded_area)
        self.shade(total_shaded_area)

    # initialize the QR code with version 2
    def init_data_v2(self):
        self.size_x = 25
        self.size_y = 25
        self.import_two_dimension([[0 for j in range(25)] for i in range(25)])
        position_detection_centers = [(3, 3), (3, 21), (21, 3)]
        alignment_detection_centers = [(18, 18)]
        total_shaded_area = []
        for position_detection_center in position_detection_centers:
            shaded_area, wight_area = self.position_detection_area(position_detection_center)
            total_shaded_area.extend(shaded_area)
        for alignment_detection_center in alignment_detection_centers:
            shaded_area, wight_area = self.alignment_detection_area(alignment_detection_center)
            total_shaded_area.extend(shaded_area)
        self.shade(total_shaded_area)


if __name__ == '__main__':
    qr_code_v1 = QRCode(version=1)
    qr_code_v1_list = qr_code_v1.to_two_dimension_list()
    qr_code_v2 = QRCode(version=2)
    qr_code_v2_list = qr_code_v2.to_two_dimension_list()
    binary_square = BinarySquare()
    # binary_square.init_random_data()
    binary_square.init_data_v2_with_random()
    # binary_square.rotate(clockwise_angle=90)
    binary_square_list = binary_square.to_two_dimension_list()
    drawMatrix(binary_square_list)
    result, v = binary_square.find_qr_code()
    drawMatrix(result)
