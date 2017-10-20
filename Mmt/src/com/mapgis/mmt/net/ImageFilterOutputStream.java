package com.mapgis.mmt.net;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageFilterOutputStream extends FilterOutputStream {
	private final ByteParse.a a;
	private final int b;
	private byte[] c = null;
	private int d = 0;

	private static byte[] e = new byte[0];

	public ImageFilterOutputStream(OutputStream paramOutputStream, int paramInt) {
		this(paramOutputStream, paramInt, true);
	}

	public ImageFilterOutputStream(OutputStream paramOutputStream, int paramInt,
			boolean paramBoolean) {
		super(paramOutputStream);
		this.b = paramInt;
		if (paramBoolean)
			this.a = new ByteParse.c(paramInt, null);
		else
			this.a = new ByteParse.b(paramInt, null);
	}

	public void write(int b) throws IOException {
		if (this.c == null) {
			this.c = new byte[1024];
		}
		if (this.d >= this.c.length) {
			a(this.c, 0, this.d, false);
			this.d = 0;
		}
		this.c[(this.d++)] = (byte) b;
	}

	private void a() throws IOException {
		if (this.d > 0) {
			a(this.c, 0, this.d, false);
			this.d = 0;
		}
	}

	public void write(byte[] b, int off, int len) throws IOException {
		if (len <= 0)
			return;
		a();
		a(b, off, len, false);
	}

	public void close() throws IOException {
		IOException localObject = null;
		try {
			a();
			a(e, 0, 0, true);
		} catch (IOException localIOException1) {
			localObject = localIOException1;
		}
		try {
			if ((this.b & 0x10) == 0)
				this.out.close();
			else
				this.out.flush();
		} catch (IOException localIOException2) {
			if (localObject != null) {
				localObject = localIOException2;
			}
		}

		if (localObject != null)
			throw localObject;
	}

	private void a(byte[] paramArrayOfByte, int paramInt1, int paramInt2,
			boolean paramBoolean) throws IOException {
		this.a.a = a(this.a.a, this.a.a(paramInt2));
		if (!this.a.a(paramArrayOfByte, paramInt1, paramInt2, paramBoolean)) {
			throw new IOException("bad base-64");
		}
		this.out.write(this.a.a, 0, this.a.b);
	}

	private byte[] a(byte[] paramArrayOfByte, int paramInt) {
		if ((paramArrayOfByte == null) || (paramArrayOfByte.length < paramInt)) {
			return new byte[paramInt];
		}
		return paramArrayOfByte;
	}
}