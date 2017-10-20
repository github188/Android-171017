package com.mapgis.mmt.module.gps.trans;

/**
 * 矩阵操作
 * 
 * @author Zoro
 * 
 */
class NNMatrix {
	public int row, col;
	public double[][] Matrix;

	public NNMatrix(int Mrow, int Mcol) // 指定行列数创建矩阵，初始值为0矩阵
	{
		row = Mrow;
		col = Mcol;

		Matrix = new double[row][col];

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				Matrix[i][j] = 0;
			}
		}
	}

	public static NNMatrix Add(NNMatrix m1, NNMatrix m2) // 矩阵加法
	{
		if (m1.row == m2.row && m1.col == m2.col) {
			for (int i = 0; i < m1.row; i++) {
				for (int j = 0; j < m2.col; j++) {
					m1.Matrix[i][j] += m2.Matrix[i][j];
				}
			}
		}
		return (m1);
	}

	public static NNMatrix Add(NNMatrix m1, double m2) // 矩阵加常量
	{
		for (int i = 0; i < m1.row; i++) {
			for (int j = 0; j < m1.col; j++) {
				m1.Matrix[i][j] += m2;
			}
		}
		return (m1);
	}

	public static NNMatrix Subtract(NNMatrix m1, NNMatrix m2) // 矩阵减法
	{
		if (m1.row == m2.row && m1.col == m2.col) {
			for (int i = 0; i < m1.row; i++) {
				for (int j = 0; j < m2.col; j++) {
					m1.Matrix[i][j] -= m2.Matrix[i][j];
				}
			}
		}
		return (m1);
	}

	public static NNMatrix Multiplication(NNMatrix m1, NNMatrix m2)// 矩阵乘法
	{
		int m3r = m1.row;
		int m3c = m2.col;
		NNMatrix m3 = new NNMatrix(m3r, m3c);

		if (m1.col == m2.row) {
			double value = 0.0;
			for (int i = 0; i < m3r; i++) {
				for (int j = 0; j < m3c; j++) {
					value = 0.0f;
					for (int ii = 0; ii < m1.col; ii++) {
						value += m1.Matrix[i][ii] * m2.Matrix[ii][j];
					}

					m3.Matrix[i][j] = value;
				}
			}
		}
		/*
		 * else throw new Exception("矩阵的行/列数不匹配。");
		 */

		return m3;
	}

	public static NNMatrix Multiplication(NNMatrix m1, double m2) // 矩阵乘以常量
	{
		for (int i = 0; i < m1.row; i++) {
			for (int j = 0; j < m1.col; j++) {
				m1.Matrix[i][j] *= m2;
			}
		}
		return (m1);
	}

	public static NNMatrix Transpos(NNMatrix srcm) // 矩阵转秩
	{

		NNMatrix tmpm = new NNMatrix(srcm.col, srcm.row);
		for (int i = 0; i < srcm.row; i++) {
			for (int j = 0; j < srcm.col; j++) {
				if (i != j) {
					tmpm.Matrix[j][i] = srcm.Matrix[i][j];
				} else {
					tmpm.Matrix[i][j] = srcm.Matrix[i][j];
				}
			}
		}
		return tmpm;
	}

	private static void swaper(double m1, double m2) // 交换
	{
		double sw;
		sw = m1;
		m1 = m2;
		m2 = sw;
	}

	/**
	 * 实矩阵求逆的全选主元高斯－约当法
	 * 
	 */
	public static NNMatrix Invers(NNMatrix srcm) // 矩阵求逆
	{
		int rhc = srcm.row;
		if (srcm.row == srcm.col) {
			int[] iss = new int[rhc];
			int[] jss = new int[rhc];
			double fdet = 1;
			double f = 1;
			// 消元
			for (int k = 0; k < rhc; k++) {
				double fmax = 0;
				for (int i = k; i < rhc; i++) {
					for (int j = k; j < rhc; j++) {
						f = Math.abs(srcm.Matrix[i][j]);
						if (f > fmax) {
							fmax = f;
							iss[k] = i;
							jss[k] = j;
						}
					}
				}

				if (iss[k] != k) {
					f = -f;
					for (int ii = 0; ii < rhc; ii++) {
						swaper(srcm.Matrix[k][ii], srcm.Matrix[iss[k]][ii]);
					}
				}

				if (jss[k] != k) {
					f = -f;
					for (int ii = 0; ii < rhc; ii++) {
						swaper(srcm.Matrix[k][ii], srcm.Matrix[jss[k]][ii]);
					}
				}

				fdet *= srcm.Matrix[k][k];
				srcm.Matrix[k][k] = 1.0 / srcm.Matrix[k][k];
				for (int j = 0; j < rhc; j++) {
					if (j != k) {
						srcm.Matrix[k][j] *= srcm.Matrix[k][k];
					}
				}

				for (int i = 0; i < rhc; i++) {
					if (i != k) {
						for (int j = 0; j < rhc; j++) {
							if (j != k) {
								srcm.Matrix[i][j] = srcm.Matrix[i][j] - srcm.Matrix[i][k] * srcm.Matrix[k][j];
							}
						}
					}
				}

				for (int i = 0; i < rhc; i++) {
					if (i != k) {
						srcm.Matrix[i][k] *= -srcm.Matrix[k][k];
					}
				}
			}
			// 调整恢复行列次序
			for (int k = rhc - 1; k >= 0; k--) {
				if (jss[k] != k) {
					for (int ii = 0; ii < rhc; ii++) {
						swaper(srcm.Matrix[k][ii], srcm.Matrix[jss[k]][ii]);
					}
				}
				if (iss[k] != k) {
					for (int ii = 0; ii < rhc; ii++) {
						swaper(srcm.Matrix[k][ii], srcm.Matrix[iss[k]][ii]);
					}
				}
			}
		}

		return srcm;

	}

	public String MatrixPrint() // 矩阵输出
	{
		String tmprst;
		tmprst = "\n";
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				tmprst += Matrix[i][j] + "\t";
			}
			tmprst += "\n";
		}
		return tmprst;
	}
}