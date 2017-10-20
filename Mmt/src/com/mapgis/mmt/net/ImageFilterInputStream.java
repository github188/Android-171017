package com.mapgis.mmt.net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageFilterInputStream extends FilterInputStream {

	private final ByteParse.a a;
	private static byte[] b = new byte[0];
	private boolean d;
	private byte[] e;
	private int f;
	private int g;

	public ImageFilterInputStream(InputStream paramInputStream, int paramInt) {
		this(paramInputStream, paramInt, false);
	}

	public ImageFilterInputStream(InputStream paramInputStream, int paramInt, boolean paramBoolean) {
		super(paramInputStream);
		this.d = false;
		this.e = new byte[2048];
		if (paramBoolean)
			this.a = new ByteParse.c(paramInt, null);
		else {
			this.a = new ByteParse.b(paramInt, null);
		}
		this.a.a = new byte[this.a.a(2048)];
		this.f = 0;
		this.g = 0;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		this.in.close();
		this.e = null;
	}

	@Override
	public int available() {
		return this.g - this.f;
	}

	@Override
	public long skip(long n) throws IOException {
		if (this.f >= this.g) {
			a();
		}
		if (this.f >= this.g) {
			return 0L;
		}
		long l = Math.min(n, this.g - this.f);
		this.f = (int) (this.f + l);
		return l;
	}

	@Override
	public int read() throws IOException {
		if (this.f >= this.g) {
			a();
		}
		if (this.f >= this.g) {
			return -1;
		}
		return this.a.a[(this.f++)];
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (this.f >= this.g) {
			a();
		}
		if (this.f >= this.g) {
			return -1;
		}
		int i = Math.min(len, this.g - this.f);
		System.arraycopy(this.a.a, this.f, b, off, i);
		this.f += i;
		return i;
	}

	private void a() throws IOException {
		if (this.d)
			return;
		int i = this.in.read(this.e);
		boolean bool;
		if (i == -1) {
			this.d = true;
			bool = this.a.a(b, 0, 0, true);
		} else {
			bool = this.a.a(this.e, 0, i, false);
		}
		if (!bool) {
			throw new IOException("bad base-64");
		}
		this.g = this.a.b;
		this.f = 0;
	}
}
