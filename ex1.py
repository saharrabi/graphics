# Copyright (c) 2019 Mattan Serry,
# Computer Graphics and Vision Lab, School of Computer Science, Tel Aviv University
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
from collections import Counter
from math import sqrt, ceil, floor, radians, cos, sin
from os import remove
from sys import float_info, version_info
from random import randint, uniform
from time import process_time, sleep
from unittest import TestCase
assert version_info >= (3, 7)
# from cv2 import imread, cvtColor, COLOR_BGR2RGB
from IPython.display import HTML, display, Image
from matplotlib import rcParams
from matplotlib.pyplot import show, figure
from numpy import ndarray, iinfo, full, clip, average, array, empty, errstate, uint
8
from PIL import Image as PILImage
from scipy.stats import norm as normal_dist
from skimage import __version__ as skimage_version
assert skimage_version >= '0.16.2'
display(HTML("<style>.container { width:100% !important; }</style>"))
class Timer:
def __enter__(self):
self.start = process_time()
print('*'*16)
return self
def __exit__(self, *args):
self.end = process_time()
self.interval = self.end - self.start
print(f'Elapsed: {self.interval:.2f} seconds')
print('*'*16)
class MyImage:
def __init__(self, tensor: ndarray):
self._tensor = tensor
self._height, self._width, self._channels = tensor.shape
self. time = None
