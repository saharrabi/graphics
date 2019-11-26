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
from numpy import ndarray, iinfo, full, clip, average, array, empty, errstate, uint8
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
        self. time = None

#In2
    def clip_to_range(z: float, *, max_dim: int):
        return clip(z, 0, max_dim - 1)
    def clip_and_round(z: float, *, max_dim: int):
        return clip_to_range(z, max_dim=max_dim).round().astype(int)
#1.1
#In3
    def nearest_neighbour(height: int, width: int, y: float, x: float) -> PartialPixels:
        x=clip_and_round(width,x)
        y=clip_and_round(height,y)
        return PartialPixel(x,y)

class NNTest(TestCase):
    def test(self):
        height, width = 768, 1024
        y1, x1 = -3.3, 58.12
        y2, x2 = 674.91, 3007.8
        nn1 = nearest_neighbour(height=height, width=width, y=y1, x=x1)
        nn2 = nearest_neighbour(height=height, width=width, y=y2, x=x2)
        print(nn1)
        print(nn2)
        # assert return type is class PartialPixels
        self.assertTrue(isinstance(nn1, PartialPixels))
        self.assertTrue(isinstance(nn2, PartialPixels))
        # assert that NN maps to exactly one pixel (the rounded), and the weight is as expected
        self.assertTrue({(0, 58): 1.0} == nn1)
        self.assertTrue({(675, 1023): 1.0} == nn2)
#In4
def bilinear(height: int, width: int, y: float, x: float) -> PartialPixels:
        
class BLTest(TestCase):
    def test(self):
        height, width = 768, 1024
        y1, x1 = 50.0, 60.0
        y2, x2 = 50.25, 60.0
        y3, x3 = 50.25, 60.50
        bl1 = bilinear(height=height, width=width, y=y1, x=x1)
        bl2 = bilinear(height=height, width=width, y=y2, x=x2)
        bl3 = bilinear(height=height, width=width, y=y3, x=x3)
        print(bl1)
        print(bl2)
        print(bl3)
        bl1.normalize()
        bl2.normalize()
        bl3.normalize()
        print(bl1)
        print(bl2)
        print(bl3)
    # assert return type is class PartialPixels
        self.assertTrue(isinstance(bl1, PartialPixels) and isinstance(bl2, PartialPixels) and isinstance(bl2, PartialPixels))
        # assert that NN maps to exactly one pixel (the rounded), and the weight isas expected
        self.assertTrue({(50, 60): 1.0,} == bl1)
        self.assertTrue(
        {
        (50, 60): 0.75,
        (51, 60): 0.25,
        } == bl2
        )
        self.assertTrue(
        {
        (50, 60): 0.375,
        (51, 60): 0.125,
        (50, 61): 0.375,
        (51, 61): 0.125,
        } == bl3
        )

#In5
def parametric_gaussian(height: int, width: int, y: float, x: float, std: float, d:
float, epsilon=float_info.epsilon):
    return result

from functools import partial
gaussian03 = partial(parametric_gaussian, std=0.3, d=0.025)
gaussian03.__name__ = 'gaussian 0.3'

class PGTest(TestCase):
    def test(self):
        height, width = 768, 1024
        y1, x1 = 0.001, 10.5
        pg = gaussian03(height=height, width=width, y=y1, x=x1)
        print(pg)
        pg.normalize()
        print(pg)
        # assert return type is class PartialPixels
        self.assertTrue(isinstance(pg, PartialPixels))
        # Verify values of the Gaussian sampling
        self.assertAlmostEqual(pg[(0, 10)], 0.5, places=2)
        self.assertAlmostEqual(pg[(0, 11)], 0.5, places=2)
        self.assertGreater(pg[(0, 11)], pg[(0, 10)])
        self.assertAlmostEqual(pg[(1, 10)], 0.0, places=2)
        self.assertAlmostEqual(pg[(1, 11)], 0.0, places=2)
        self.assertAlmostEqual(pg[(1, 10)], pg[(1, 11)])

#In6
def calculate_all_partial_pixels_by_method(height: int, width: int, method: callable, backward_map: dict) -> ndarray:
    result = {}
    for (new_y, new_x), (old_y, old_x) in backward_map.items():
        result[(new_y, new_x)] = method(height, width, old_y, old_x)
    return result