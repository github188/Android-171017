package com.mapgis.mmt.net;

import java.io.UnsupportedEncodingException;

public class ByteParse {
	public static final int a = 0;
	public static final int b = 1;
	public static final int c = 2;
	public static final int d = 4;
	public static final int e = 8;
	public static final int f = 16;

	public static byte[] a(String paramString, int paramInt) {
		return a(paramString.getBytes(), paramInt);
	}

	public static byte[] a(byte[] paramArrayOfByte, int paramInt) {
		return a(paramArrayOfByte, 0, paramArrayOfByte.length, paramInt);
	}

	public static byte[] a(byte[] paramArrayOfByte, int paramInt1,
			int paramInt2, int paramInt3) {
		b localb = new b(paramInt3, new byte[paramInt2 * 3 / 4]);

		if (!localb.a(paramArrayOfByte, paramInt1, paramInt2, true)) {
			throw new IllegalArgumentException("bad base-64");
		}

		if (localb.b == localb.a.length) {
			return localb.a;
		}

		byte[] arrayOfByte = new byte[localb.b];
		System.arraycopy(localb.a, 0, arrayOfByte, 0, localb.b);
		return arrayOfByte;
	}

	public static String b(byte[] paramArrayOfByte, int paramInt) {
		try {
			return new String(c(paramArrayOfByte, paramInt), "US-ASCII");
		} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
			throw new AssertionError(localUnsupportedEncodingException);
		}
	}

	public static String b(byte[] paramArrayOfByte, int paramInt1,
			int paramInt2, int paramInt3) {
		try {
			return new String(c(paramArrayOfByte, paramInt1, paramInt2,
					paramInt3), "US-ASCII");
		} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
			throw new AssertionError(localUnsupportedEncodingException);
		}
	}

	public static byte[] c(byte[] paramArrayOfByte, int paramInt) {
		return c(paramArrayOfByte, 0, paramArrayOfByte.length, paramInt);
	}

	public static byte[] c(byte[] paramArrayOfByte, int paramInt1,
			int paramInt2, int paramInt3) {
		c localc = new c(paramInt3, null);

		int i = paramInt2 / 3 * 4;

		if (localc.e) {
			if (paramInt2 % 3 > 0)
				i += 4;
		} else {
			switch (paramInt2 % 3) {
			case 0:
				break;
			case 1:
				i += 2;
				break;
			case 2:
				i += 3;
			}

		}

		if ((localc.f) && (paramInt2 > 0)) {
			i += ((paramInt2 - 1) / 57 + 1) * (localc.g ? 2 : 1);
		}

		localc.a = new byte[i];
		localc.a(paramArrayOfByte, paramInt1, paramInt2, true);

		if ((!localc.g) && (localc.b != i))
			throw new AssertionError();

		return localc.a;
	}

	static class c extends ByteParse.a {
		public static final int c = 19;
		private static final byte[] i;
		private static final byte[] j;
		private final byte[] k;
		int d;
		private int l;
		public final boolean e;
		public final boolean f;
		public final boolean g;
		private final byte[] m;

		public c(int paramInt, byte[] paramArrayOfByte) {
			this.a = paramArrayOfByte;

			this.e = ((paramInt & 0x1) == 0);
			this.f = ((paramInt & 0x2) == 0);
			this.g = ((paramInt & 0x4) != 0);
			this.m = ((paramInt & 0x8) == 0 ? i : j);

			this.k = new byte[2];
			this.d = 0;

			this.l = (this.f ? 19 : -1);
		}

		public int a(int paramInt) {
			return paramInt * 8 / 5 + 10;
		}

		@Override
		public boolean a(byte[] paramArrayOfByte, int paramInt1, int paramInt2,
				boolean paramBoolean) {
			byte[] arrayOfByte1 = this.m;
			byte[] arrayOfByte2 = this.a;
			int n = 0;
			int i1 = this.l;

			int i2 = paramInt1;
			paramInt2 += paramInt1;
			int i3 = -1;

			switch (this.d) {
			case 0:
				break;
			case 1:
				if (i2 + 2 > paramInt2) {
					break;
				}
				i3 = (this.k[0] & 0xFF) << 16
						| (paramArrayOfByte[(i2++)] & 0xFF) << 8
						| paramArrayOfByte[(i2++)] & 0xFF;

				this.d = 0;
				break;
			case 2:
				if (i2 + 1 > paramInt2)
					break;
				i3 = (this.k[0] & 0xFF) << 16 | (this.k[1] & 0xFF) << 8
						| paramArrayOfByte[(i2++)] & 0xFF;

				this.d = 0;
			}

			if (i3 != -1) {
				arrayOfByte2[(n++)] = arrayOfByte1[(i3 >> 18 & 0x3F)];
				arrayOfByte2[(n++)] = arrayOfByte1[(i3 >> 12 & 0x3F)];
				arrayOfByte2[(n++)] = arrayOfByte1[(i3 >> 6 & 0x3F)];
				arrayOfByte2[(n++)] = arrayOfByte1[(i3 & 0x3F)];
				i1--;
				if (i1 == 0) {
					if (this.g)
						arrayOfByte2[(n++)] = 13;
					arrayOfByte2[(n++)] = 10;
					i1 = 19;
				}

			}

			while (i2 + 3 <= paramInt2) {
				i3 = (paramArrayOfByte[i2] & 0xFF) << 16
						| (paramArrayOfByte[(i2 + 1)] & 0xFF) << 8
						| paramArrayOfByte[(i2 + 2)] & 0xFF;

				arrayOfByte2[n] = arrayOfByte1[(i3 >> 18 & 0x3F)];
				arrayOfByte2[(n + 1)] = arrayOfByte1[(i3 >> 12 & 0x3F)];
				arrayOfByte2[(n + 2)] = arrayOfByte1[(i3 >> 6 & 0x3F)];
				arrayOfByte2[(n + 3)] = arrayOfByte1[(i3 & 0x3F)];
				i2 += 3;
				n += 4;
				i1--;
				if (i1 == 0) {
					if (this.g)
						arrayOfByte2[(n++)] = 13;
					arrayOfByte2[(n++)] = 10;
					i1 = 19;
				}
			}

			if (paramBoolean) {
				int i4;
				if (i2 - this.d == paramInt2 - 1) {
					i4 = 0;
					i3 = ((this.d > 0 ? this.k[(i4++)]
							: paramArrayOfByte[(i2++)]) & 0xFF) << 4;
					this.d -= i4;
					arrayOfByte2[(n++)] = arrayOfByte1[(i3 >> 6 & 0x3F)];
					arrayOfByte2[(n++)] = arrayOfByte1[(i3 & 0x3F)];
					if (this.e) {
						arrayOfByte2[(n++)] = 61;
						arrayOfByte2[(n++)] = 61;
					}
					if (this.f) {
						if (this.g)
							arrayOfByte2[(n++)] = 13;
						arrayOfByte2[(n++)] = 10;
					}
				} else if (i2 - this.d == paramInt2 - 2) {
					i4 = 0;
					i3 = ((this.d > 1 ? this.k[(i4++)]
							: paramArrayOfByte[(i2++)]) & 0xFF) << 10
							| ((this.d > 0 ? this.k[(i4++)]
									: paramArrayOfByte[(i2++)]) & 0xFF) << 2;

					this.d -= i4;
					arrayOfByte2[(n++)] = arrayOfByte1[(i3 >> 12 & 0x3F)];
					arrayOfByte2[(n++)] = arrayOfByte1[(i3 >> 6 & 0x3F)];
					arrayOfByte2[(n++)] = arrayOfByte1[(i3 & 0x3F)];
					if (this.e) {
						arrayOfByte2[(n++)] = 61;
					}
					if (this.f) {
						if (this.g)
							arrayOfByte2[(n++)] = 13;
						arrayOfByte2[(n++)] = 10;
					}
				} else if ((this.f) && (n > 0) && (i1 != 19)) {
					if (this.g)
						arrayOfByte2[(n++)] = 13;
					arrayOfByte2[(n++)] = 10;
				}

//				if ((!h) && (this.d != 0))
//					throw new AssertionError();
//				if ((!h) && (i2 != paramInt2))
//					throw new AssertionError();

			} else if (i2 == paramInt2 - 1) {
				this.k[(this.d++)] = paramArrayOfByte[i2];
			} else if (i2 == paramInt2 - 2) {
				this.k[(this.d++)] = paramArrayOfByte[i2];
				this.k[(this.d++)] = paramArrayOfByte[(i2 + 1)];
			}

			this.b = n;
			this.l = i1;

			return true;
		}

		static {
			i = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76,
					77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97,
					98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
					110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121,
					122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };

			j = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76,
					77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97,
					98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
					110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121,
					122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95 };
		}
	}

	static class b extends ByteParse.a {
		private static final int[] c = { -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60,
				61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
				10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
				-1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
				36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1 };

		private static final int[] d = { -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60,
				61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
				10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
				-1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
				36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1 };
//		private static final int e = -1;
//		private static final int f = -2;
		private int g;
		private int h;
		private final int[] i;

		public b(int paramInt, byte[] paramArrayOfByte) {
			this.a = paramArrayOfByte;

			this.i = ((paramInt & 0x8) == 0 ? c : d);
			this.g = 0;
			this.h = 0;
		}

		public int a(int paramInt) {
			return paramInt * 3 / 4 + 10;
		}

		public boolean a(byte[] paramArrayOfByte, int paramInt1, int paramInt2,
				boolean paramBoolean) {
			if (this.g == 6)
				return false;

			int j = paramInt1;
			paramInt2 += paramInt1;

			int k = this.g;
			int m = this.h;
			int n = 0;
			byte[] arrayOfByte = this.a;
			int[] arrayOfInt = this.i;

			while (j < paramInt2) {
				if (k == 0) {
					while ((j + 4 <= paramInt2)
							&& ((m = arrayOfInt[(paramArrayOfByte[j] & 0xFF)] << 18
									| arrayOfInt[(paramArrayOfByte[(j + 1)] & 0xFF)] << 12
									| arrayOfInt[(paramArrayOfByte[(j + 2)] & 0xFF)] << 6
									| arrayOfInt[(paramArrayOfByte[(j + 3)] & 0xFF)]) >= 0)) {
						arrayOfByte[(n + 2)] = (byte) m;
						arrayOfByte[(n + 1)] = (byte) (m >> 8);
						arrayOfByte[n] = (byte) (m >> 16);
						n += 3;
						j += 4;
					}
					if (j >= paramInt2) {
						break;
					}

				}

				int i1 = arrayOfInt[(paramArrayOfByte[(j++)] & 0xFF)];

				switch (k) {
				case 0:
					if (i1 >= 0) {
						m = i1;
						k++;
					} else {
						if (i1 == -1)
							break;
						this.g = 6;
						return false;
					}

				case 1:
					if (i1 >= 0) {
						m = m << 6 | i1;
						k++;
					} else {
						if (i1 == -1)
							break;
						this.g = 6;
						return false;
					}

				case 2:
					if (i1 >= 0) {
						m = m << 6 | i1;
						k++;
					} else if (i1 == -2) {
						arrayOfByte[(n++)] = (byte) (m >> 4);
						k = 4;
					} else {
						if (i1 == -1)
							break;
						this.g = 6;
						return false;
					}

				case 3:
					if (i1 >= 0) {
						m = m << 6 | i1;
						arrayOfByte[(n + 2)] = (byte) m;
						arrayOfByte[(n + 1)] = (byte) (m >> 8);
						arrayOfByte[n] = (byte) (m >> 16);
						n += 3;
						k = 0;
					} else if (i1 == -2) {
						arrayOfByte[(n + 1)] = (byte) (m >> 2);
						arrayOfByte[n] = (byte) (m >> 10);
						n += 2;
						k = 5;
					} else {
						if (i1 == -1)
							break;
						this.g = 6;
						return false;
					}

				case 4:
					if (i1 == -2) {
						k++;
					} else {
						if (i1 == -1)
							break;
						this.g = 6;
						return false;
					}

				case 5:
					if (i1 == -1)
						break;
					this.g = 6;
					return false;
				}

			}

			if (!paramBoolean) {
				this.g = k;
				this.h = m;
				this.b = n;
				return true;
			}

			switch (k) {
			case 0:
				break;
			case 1:
				this.g = 6;
				return false;
			case 2:
				arrayOfByte[(n++)] = (byte) (m >> 4);
				break;
			case 3:
				arrayOfByte[(n++)] = (byte) (m >> 10);
				arrayOfByte[(n++)] = (byte) (m >> 2);
				break;
			case 4:
				this.g = 6;
				return false;
			case 5:
			}

			this.g = k;
			this.b = n;
			return true;
		}
	}

	static abstract class a {
		public byte[] a;
		public int b;

		public abstract boolean a(byte[] paramArrayOfByte, int paramInt1,
				int paramInt2, boolean paramBoolean);

		public abstract int a(int paramInt);
	}
}
