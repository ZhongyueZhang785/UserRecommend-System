# -*- coding: utf-8 -*-
"""
Created on Sat Oct  3 22:45:57 2021

@author: ZHANG ZHONGYUE
"""
import numpy
from .QR_Encode import makeVersion2
import numpy as np
from tkinter import *
########################################
# Draw matrix for visualization
########################################
def drawMatrix(board):
    def drawboard(board, colors, startx=0, starty=0, cellwidth=10):
        width = 2 * startx + len(board) * cellwidth
        height = 2 * starty + len(board) * cellwidth
        canvas.config(width=width, height=height)
        for i in range(len(board)):
            for j in range(len(board)):
                index = board[j][i]
                color = colors[index]
                cellx = startx + i * cellwidth
                celly = starty + j * cellwidth
                canvas.create_rectangle(cellx, celly, cellx + cellwidth, celly + cellwidth,
                                        fill=color, outline="black")
        canvas.update()

    root = Tk()
    canvas = Canvas(root, bg="white")
    canvas.pack()
    colors = ['white', 'black', 'grey']
    drawboard(board, colors)
    root.mainloop()

########################################
# Draw current position of 25*25 matrix
########################################
def judgePosition(QR_matrix):
    QR_matrix = np.mat(QR_matrix)
    start=0
    end = 8
    colum = 0
    QR_matrix[start:end,colum][1,0,1,0,0,1,0]
    little_square_0 = QR_matrix[16:21,16:21]
    little_square_1 = QR_matrix[16:21, 4:9]
    little_square_2 = QR_matrix[4:9,4:9]
    little_square_3 = QR_matrix[4:9, 16:21]
    judge_square = np.mat([[1,1,1,1,1],[1,0,0,0,1],[1,0,1,0,1],[1,0,0,0,1],[1,1,1,1,1]])

    if(np.all((little_square_0-judge_square) == 0)):
        #print("0")
        return 0
    elif np.all((little_square_1-judge_square)==0):
        #print("1")
        return 1
    elif np.all((little_square_2-judge_square)==0):
        #print("2")
        return 2
    elif np.all((little_square_3-judge_square)==0):
        #print("3")
        return 3
########################################
# rotate 25*25 matrix to right position
########################################
def rotatePosition(QR_matrix):
    num = judgePosition(QR_matrix)
    QR_matrix = numpy.rot90(QR_matrix, k=num)
    return QR_matrix



def main():
    #test rotate
    QR_matrix = makeVersion2()
    QR_matrix = numpy.rot90(QR_matrix, k=3)
    drawMatrix(QR_matrix)
    print("begin rotate")
    QR_matrix=rotatePosition(QR_matrix)#Matrix at right position
    drawMatrix(QR_matrix)
if __name__ == '__main__':
    main()