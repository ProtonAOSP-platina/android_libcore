/*
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.nio;


class ByteBufferAsDoubleBuffer
    extends DoubleBuffer {            // package-private

    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;
    private final boolean isReadOnly;

    ByteBufferAsDoubleBuffer(ByteBuffer bb, ByteOrder order) {   // package-private
        this(bb, order, false);
    }

    ByteBufferAsDoubleBuffer(ByteBuffer bb, ByteOrder order, boolean isReadOnly) {   // package-private
        super(-1, 0,
              bb.remaining() >> 3,
              bb.remaining() >> 3);
        this.bb = bb;
        this.order = order;
        this.isReadOnly = isReadOnly;
        int cap = this.capacity();
        this.limit(cap);
        int pos = this.position();
        assert (pos <= cap);
        offset = pos;
    }

    ByteBufferAsDoubleBuffer(ByteBuffer bb,
                             int mark, int pos, int lim, int cap,
                             int off, ByteOrder order) {
        this(bb, mark, pos, lim, cap, off, order, false);
    }

    ByteBufferAsDoubleBuffer(ByteBuffer bb,
                             int mark, int pos, int lim, int cap,
                             int off, ByteOrder order, boolean isReadOnly) {
        super(mark, pos, lim, cap);
        this.bb = bb;
        this.order = order;
        this.isReadOnly = isReadOnly;
        offset = off;
    }

    public DoubleBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 3) + offset;
        assert (off >= 0);
        return new ByteBufferAsDoubleBuffer(bb, -1, 0, rem, rem, off, order, isReadOnly);
    }

    public DoubleBuffer duplicate() {
        return new ByteBufferAsDoubleBuffer(bb,
                                            this.markValue(),
                                            this.position(),
                                            this.limit(),
                                            this.capacity(),
                                            offset,
                                            order,
                                            isReadOnly);
    }

    public DoubleBuffer asReadOnlyBuffer() {
        return new ByteBufferAsDoubleBuffer(bb,
                                            this.markValue(),
                                            this.position(),
                                            this.limit(),
                                            this.capacity(),
                                            offset,
                                            order,
                                            true);
    }

    protected int ix(int i) {
        return (i << 3) + offset;
    }

    public double get() {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            return Bits.getDoubleL(bb, ix(nextGetIndex()));
        } else {
            return Bits.getDoubleB(bb, ix(nextGetIndex()));
        }
    }

    public double get(int i) {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            return Bits.getDoubleL(bb, ix(checkIndex(i)));
        } else {
            return Bits.getDoubleB(bb, ix(checkIndex(i)));
        }
    }

    public DoubleBuffer put(double x) {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        if (order == ByteOrder.LITTLE_ENDIAN) {
            Bits.putDoubleL(bb, ix(nextPutIndex()), x);
        } else {
            Bits.putDoubleB(bb, ix(nextPutIndex()), x);
        }
        return this;
    }

    public DoubleBuffer put(int i, double x) {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        if (order == ByteOrder.LITTLE_ENDIAN) {
            Bits.putDoubleL(bb, ix(checkIndex(i)), x);
        } else {
            Bits.putDoubleB(bb, ix(checkIndex(i)), x);
        }
        return this;
    }

    public DoubleBuffer compact() {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        ByteBuffer db = bb.duplicate();
        db.limit(ix(lim));
        db.position(ix(0));
        ByteBuffer sb = db.slice();
        sb.position(pos << 3);
        sb.compact();
        position(rem);
        limit(capacity());
        discardMark();
        return this;
    }

    public boolean isDirect() {
        return bb.isDirect();
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public ByteOrder order() {
        return order;
    }
}
