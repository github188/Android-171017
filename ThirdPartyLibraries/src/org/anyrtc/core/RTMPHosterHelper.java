/**
 *  Copyright (c) 2016 The AnyRTC project authors. All Rights Reserved.
 *
 *  Please visit https://www.anyrtc.io for detail.
 *
 * The GNU General Public License is a free, copyleft license for
 * software and other kinds of works.
 *
 * The licenses for most software and other practical works are designed
 * to take away your freedom to share and change the works.  By contrast,
 * the GNU General Public License is intended to guarantee your freedom to
 * share and change all versions of a program--to make sure it remains free
 * software for all its users.  We, the Free Software Foundation, use the
 * GNU General Public License for most of our software; it applies also to
 * any other work released this way by its authors.  You can apply it to
 * your programs, too.
 * See the GNU LICENSE file for more info.
 */
package org.anyrtc.core;

/**
 * Created by Eric on 2016/7/25.
 */
public interface RTMPHosterHelper {
    //* RTMP Callback
    void OnRtmpStreamOK();
    void OnRtmpStreamReconnecting(int times);
    void OnRtmpStreamStatus(int delayMs, int netBand);
    void OnRtmpStreamFailed(int code);
    void OnRtmpStreamClosed();
}
