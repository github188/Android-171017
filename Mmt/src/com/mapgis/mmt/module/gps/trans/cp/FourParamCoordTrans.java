package com.mapgis.mmt.module.gps.trans.cp;

import com.zondy.mapgis.geometry.Dot;

/*
 * class name：FourParamCoordTrans
 * function: 通过提供的两个平面坐标系之间的对应点，计算坐标系统之间的转换参数，
 * 并直接直接提供两个坐标系之间正反转换的接口
 * */
public class FourParamCoordTrans {
	/*
	 * 直接采用四参数进行够造的,角度是弧度值
	 */
	/*
	 * public function FourParamCoordTrans(xOffset:Number,
	 * yOffset:Number,rotateAngle:Number,scale:Number) { x_off = xOffset; y_off
	 * = yOffset; m = scale; angle = rotateAngle;
	 * 
	 * // 计算方程式系数 a= x_off; b = y_off; c = m*Math.sin(angle); d =
	 * m*Math.cos(angle);
	 * 
	 * // 计算逆解算坐标的方程系数 CalcReverseEqualParams(); bCalcEqualsParams = true; }
	 */

	/*
	 * 提供坐标点对，进行四参数构造 srcX,srcY,desX,desY,分别是源坐标系和目标坐标系坐标对
	 */
	// public function FourParamCoordTrans(double[] srcX,double []srcY,double
	// []desX,double[] desY)
	public FourParamCoordTrans(double[] srcX, double[] srcY, double[] desX, double[] desY) {
		if (srcX.length < 1 || srcY.length < 1 || desX.length < 1 || desY.length < 1) {
			return;
		}

		double[][] xx;

		xx = CaleFourParam(srcX, srcY, desX, desY);
		a = xx[2][0];
		b = xx[3][0];
		c = xx[0][0];
		d = xx[1][0];

		// 计算逆解算坐标的方程系数
		CalcReverseEqualParams();
		bCalcEqualsParams = true;
	}

	// 四参数计算,整体最小二乘
	private double[][] CaleFourParam(double[] x, double[] y, double[] x1, double[] y1) // 返回的是二维数组
	{
		// double[,] X0 = new double[2, 1];//储存转换参数 abcd
		double[][] X0 = Create2DimArray(2, 1);

		// 转换前坐标x，y，转换后坐标x1，y1
		if (x.length != y.length || x.length != x1.length || x1.length != y1.length) {
			return null;
		}
		// 返回值
		double[][] xx = Create2DimArray(4, 1);

		double[][] B = Create2DimArray(2 * x.length, 2);
		double[][] BT = Create2DimArray(2, 2 * x.length);
		double[][] BTB = Create2DimArray(2, 2);
		double[][] BTl = Create2DimArray(2, 2 * x.length);
		double[][] BTB1 = Create2DimArray(2, 2);
		double[][] l = Create2DimArray(2 * x.length, 1);
		double[][] xy = Create2DimArray(2, 1);
		double xg, yg, xg1, yg1;
		xg = yg = xg1 = yg1 = 0.0;
		for (int i = 0; i < x.length; i++) {
			xg += x[i];
			yg += y[i];
			xg1 += x1[i];
			yg1 += y1[i];
		}

		xg = xg / x.length;
		yg = yg / x.length;
		xg1 = xg1 / x.length;
		yg1 = yg1 / x.length;
		for (int i = 0; i < x.length; i++) {
			B[2 * i][0] = x[i] - xg;
			B[2 * i][1] = yg - y[i];
			B[2 * i + 1][0] = -B[2 * i][1];
			B[2 * i + 1][1] = B[2 * i][0];
			l[2 * i][0] = x1[i] - xg1;
			l[2 * i + 1][0] = y1[i] - yg1;
		}
		double[][] L = l;
		while (true) {
			BT = transpose(B);
			BTB = multiply(BT, B);
			BTB1 = ReverseMatrix(BTB, 2);
			BTl = multiply(BTB1, BT);
			xy = multiply(BTl, l);
			X0[0][0] += xy[0][0];
			X0[1][0] += xy[1][0];
			if (Math.abs(xy[0][0]) < 0.000001 && Math.abs(xy[1][0]) < 0.000001) {
				break;
			} else {
				double[][] a = MinusMatrix(l, multiply(B, xy));
				double[][] xyt = transpose(xy);
				double[][] b = multiply(xyt, xy);
				b[0][0] = b[0][0] + 1;
				b = ReverseMatrix(b, 1);
				double[][] c = multiply(multiply(a, b), xyt);
				B = PlusMatrix(B, c);

				double[][] a1 = multiply(B, X0);
				l = MinusMatrix(L, a1);
			}

		}
		xx[0][0] = X0[0][0];
		xx[1][0] = X0[1][0];
		xx[2][0] = xg1 - (X0[0][0] * xg - yg * X0[1][0]);
		xx[3][0] = yg1 - (yg * X0[0][0] + xg * X0[1][0]);

		return xx;
	}

	/*
	 * 已知源坐标系下的坐标，计算目标坐标系下的坐标
	 */
	public Dot TransCoord(Dot srcPos) {
		Dot desPos = new Dot(0, 0);

		desPos.x = a + srcPos.x * c - srcPos.y * d;
		desPos.y = b + srcPos.x * d + srcPos.y * c;

		return desPos;
	}

	/*
	 * 已知目的坐标系下的坐标，反算其在源坐标系下的坐标
	 */
	public Dot TransCoordReverse(Dot desPos) {
		Dot srcPos = new Dot(0, 0);

		srcPos.x = ar + desPos.x * cr - desPos.y * dr;
		srcPos.y = br + desPos.x * dr + desPos.y * cr;

		return srcPos;
	}

	private void CalcReverseEqualParams() {
		// TODO Auto Generated method stub
		ar = -(a * c + b * d) / (c * c + d * d);
		br = (a * d - b * c) / (c * c + d * d);
		cr = c / (c * c + d * d);
		dr = -d / (c * c + d * d);
	}

	private double[][] Create2DimArray(int row, int col) {
		double[][] rowArray = new double[row][];

		for (int i = 0; i < row; i++) {
			rowArray[i] = new double[col];

			// 初始化值
			for (int j = 0; j < col; j++) {
				rowArray[i][j] = 0;
			}
		}

		return rowArray;
	}

	// 矩阵转置
	private double[][] transpose(double[][] array) {
		int x = array.length; // 读取一维长度

		if (x < 1) {
			return null;
		}

		int y = array[0].length; // 二维长度

		double[][] newArray = Create2DimArray(y, x); // 构造转置二维数组

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				newArray[j][i] = array[i][j];
			}
		}

		return newArray;
	}

	// 矩阵相乘
	private double[][] multiply(double[][] array, double[][] array1) {
		int x = array.length;// 行数
		int y = array[0].length;// 列数
		int x1 = array1.length;// 行数
		int y1 = array1[0].length;// 列数

		if (y != x1) {
			// throw new ArgumentException("矩阵相乘出现错误：前一矩阵的列数不等于后一矩阵的行数！");
			return null;
		}

		double[][] array2 = Create2DimArray(x, y1); // 构造转置二维数组

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y1; j++) {
				array2[i][j] = 0;
				for (int k = 0; k < x1; k++) {
					array2[i][j] += array[i][k] * array1[k][j];
				}
			}
		}
		return array2;
	}

	// *******************矩阵相减******************//
	private double[][] MinusMatrix(double[][] a, double[][] b) {
		int arow = a.length;// 矩阵a的行数
		int acol = a[0].length;// 矩阵a的列数
		int brow = b.length;// 矩阵b的行数
		int bcol = b[0].length;// 矩阵b的列数

		if (arow != brow || acol != bcol) {
			// throw new System.ArgumentException("两个矩阵对应行列数不相等");
			return null;
		}
		double[][] c = Create2DimArray(arow, bcol);
		for (int i = 0; i < arow; i++) {
			for (int j = 0; j < acol; j++) {
				c[i][j] = a[i][j] - b[i][j];
			}
		}
		return c;
	}

	// *******************矩阵相加******************//
	private double[][] PlusMatrix(double[][] a, double[][] b) {
		int arow = a.length;// 矩阵a的行数
		int acol = a[0].length;// 矩阵a的列数
		int brow = b.length;// 矩阵b的行数
		int bcol = b[0].length;// 矩阵b的列数

		if (arow != brow || acol != bcol) {
			// throw new System.ArgumentException("两个矩阵对应行列数不相等");
			return null;
		}
		double[][] c = Create2DimArray(arow, bcol);
		for (int i = 0; i < arow; i++) {
			for (int j = 0; j < acol; j++) {
				c[i][j] = a[i][j] + b[i][j];
			}
		}
		return c;
	}

	private double MatrixValue(double[][] MatrixList, int Level) {
		double[][] dMatrix = Create2DimArray(Level, Level);

		for (int i = 0; i < Level; i++) {
			for (int j = 0; j < Level; j++) {
				dMatrix[i][j] = MatrixList[i][j];
			}
		}
		double c, x;
		int k = 1;
		for (int i = 0, j = 0; i < Level && j < Level; i++, j++) {
			if (dMatrix[i][j] == 0) {
				int m = i;
				for (; dMatrix[m][j] == 0; m++) {
                }
				if (m == Level) {
					return 0;
				} else {
					// Row change between i-row and m-row
					for (int n = j; n < Level; n++) {
						c = dMatrix[i][n];
						dMatrix[i][n] = dMatrix[m][n];
						dMatrix[m][n] = c;
					}
					// Change value pre-value
					k = k * (-1);
				}
			}

			// Set 0 to the current column in the rows after current row
			for (int s = Level - 1; s > i; s--) {
				x = dMatrix[s][j];
				for (int t = j; t < Level; t++) {
					dMatrix[s][t] -= dMatrix[i][t] * (x / dMatrix[i][j]);
				}
			}
		}

		double sn = 1;
		for (int i = 0; i < Level; i++) {
			if (dMatrix[i][i] != 0) {
				sn *= dMatrix[i][i];
			} else {
				return 0;
			}
		}
		return k * sn;
	}

	// 矩阵求逆
	private double[][] ReverseMatrix(double[][] dMatrix, int Level) {
		double dMatrixValue = MatrixValue(dMatrix, Level);

		if (dMatrixValue == 0) {
			return null;
		}

		double[][] dReverseMatrix = Create2DimArray(Level, 2 * Level);
		double x, c;

		// Init Reverse matrix
		for (int i = 0; i < Level; i++) {
			for (int j = 0; j < 2 * Level; j++) {
				if (j < Level) {
					dReverseMatrix[i][j] = dMatrix[i][j];
				} else {
					dReverseMatrix[i][j] = 0;
				}
			}

			dReverseMatrix[i][Level + i] = 1;
		}

		for (int i = 0, j = 0; i < Level && j < Level; i++, j++) {
			if (dReverseMatrix[i][j] == 0) {
				int m = i;
				for (; dMatrix[m][j] == 0; m++) {
                }
				if (m == Level) {
					return null;
				} else {
					// Add i-row with m-row
					for (int n = j; n < 2 * Level; n++) {
						dReverseMatrix[i][n] = dReverseMatrix[i][n] + dReverseMatrix[m][n];
					}
				}
			}

			// Format the i-row with "1" start
			x = dReverseMatrix[i][j];
			if (x != 1) {
				for (int n = j; n < 2 * Level; n++) {
					if (dReverseMatrix[i][n] != 0) {
						dReverseMatrix[i][n] = dReverseMatrix[i][n] / x;
					}
				}
			}

			// Set 0 to the current column in the rows after current row
			for (int s = Level - 1; s > i; s--) {
				x = dReverseMatrix[s][j];

				for (int t = j; t < 2 * Level; t++) {
					dReverseMatrix[s][t] = dReverseMatrix[s][t] - dReverseMatrix[i][t] * x;
				}
			}
		}

		// Format the first matrix into unit-matrix
		for (int i = Level - 2; i >= 0; i--) {
			for (int j = i + 1; j < Level; j++) {
				if (dReverseMatrix[i][j] != 0) {
					c = dReverseMatrix[i][j];
					for (int n = j; n < 2 * Level; n++) {
						dReverseMatrix[i][n] = dReverseMatrix[i][n] - c * dReverseMatrix[j][n];
					}
				}
			}
		}

		double[][] dReturn = Create2DimArray(Level, Level);
		for (int i = 0; i < Level; i++) {
			for (int j = 0; j < Level; j++) {
				dReturn[i][j] = dReverseMatrix[i][j + Level];
			}
		}
		return dReturn;
	}

	// x方向偏移量，单位：米
	private double x_off;

	// y方向偏移量，单位：米
	private double y_off;

	// 尺度因子，单位，无
	private double m;

	// 旋转角度，单位弧度
	private double angle;

	/*
	 * 记录坐标变换方程的四个参数， x1 = xoffset + x0 * scale * sin(rotateAngle) - y0 * scale
	 * * cos(rotateAngle) y1 = yoffset + x0 * scale * cos(rotateAngle) + y0 *
	 * scale * sin(rotateAngle) 令： a = xoffset b = yoffset c = scale *
	 * sin(rotateAngle) d = scale * cos(rotateAngle) 得 x1 = a + x0 * c - y0 * d
	 * y1 = b + x0 * d + y0 * c
	 * 
	 * // 坐标逆算 x0 = -(a*c+b*d)/(c*c + d*d) + x1 * c/(c*c + d*d) + y1 * d/(c*c +
	 * d*d) y0 = (a*d-b*c)/(c*c + d*d) - x1 * d/(c*c + d*d) + y1 * c/(c*c + d*d)
	 * 故 ar = -(a*c+b*d)/(c*c + d*d) br = (a*d-b*c)/(c*c + d*d) cr = c/(c*c +
	 * d*d) dr = -d/(c*c + d*d)
	 */
	// 正算方程式系数
	private double a, b, c, d;
	// 反算方程式系数
	private double ar, br, cr, dr;

	/*
	 * 是否解算了方程系数
	 */
	private Boolean bCalcEqualsParams = false;
}
