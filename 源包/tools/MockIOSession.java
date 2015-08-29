/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools;

import java.net.SocketAddress;
import java.util.Set;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

/**
 * Represents a mock version of an IOSession to use a MapleClient instance
 * without an active connection (faekchar, etc).
 *
 * Most methods return void, or when they return something, null. Therefore,
 * this class is mostly undocumented, due to the fact that each and every
 * function does squat.
 *
 * @author Frz
 * @since Revision 518
 * @version 1.0
 */
public class MockIOSession implements IoSession {

    /**
     * Does nothing.
     *
     * @return
     */
    @Override
    public IoSessionConfig getConfig() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @return
     */
    @Override
    public IoFilterChain getFilterChain() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @return
     */
    @Override
    public IoHandler getHandler() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @return
     */
    @Override
    public SocketAddress getLocalAddress() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @return
     */
    @Override
    public SocketAddress getRemoteAddress() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @return
     */
    @Override
    public IoService getService() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @return
     */
    @Override
    public SocketAddress getServiceAddress() {
        return null;
    }

    /**
     * Does nothing.
     *
     * @param message
     * @param remoteAddress
     * @return
     */
    @Override
    public WriteFuture write(Object message, SocketAddress remoteAddress) {
        return null;
    }

    /**
     * "Fake writes" a packet to the client, only running the OnSend event of
     * the packet.
     *
     * @param message
     * @return
     */
    @Override
    public WriteFuture write(Object message) {
        return null;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public WriteRequestQueue getWriteRequestQueue() {
        return null;
    }

    @Override
    public TransportMetadata getTransportMetadata() {
        return null;
    }

    @Override
    public ReadFuture read() {
        return null;
    }

    @Override
    public CloseFuture close(boolean bln) {
        return null;
    }

    @Override
    public Object getAttribute(Object o) {
        return null;
    }

    @Override
    public Object getAttribute(Object o, Object o1) {
        return null;
    }

    @Override
    public Object setAttribute(Object o, Object o1) {
        return null;
    }

    @Override
    public Object setAttribute(Object o) {
        return null;
    }

    @Override
    public Object setAttributeIfAbsent(Object o, Object o1) {
        return null;
    }

    @Override
    public Object setAttributeIfAbsent(Object o) {
        return null;
    }

    @Override
    public Object removeAttribute(Object o) {
        return null;
    }

    @Override
    public boolean removeAttribute(Object o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean replaceAttribute(Object o, Object o1, Object o2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsAttribute(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Object> getAttributeKeys() {
        return null;
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isClosing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CloseFuture getCloseFuture() {
        return null;
    }

    @Override
    public void setCurrentWriteRequest(WriteRequest wr) {

    }

    @Override
    public void suspendRead() {

    }

    @Override
    public void suspendWrite() {

    }

    @Override
    public void resumeRead() {

    }

    @Override
    public void resumeWrite() {

    }

    @Override
    public boolean isReadSuspended() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isWriteSuspended() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateThroughput(long l, boolean bln) {

    }

    @Override
    public long getReadBytes() {
        return -1;
    }

    @Override
    public long getWrittenBytes() {
        return -1;
    }

    @Override
    public long getReadMessages() {
        return -1;
    }

    @Override
    public long getWrittenMessages() {
        return -1;
    }

    @Override
    public double getReadBytesThroughput() {
        return -1;
    }

    @Override
    public double getWrittenBytesThroughput() {
        return -1;
    }

    @Override
    public double getReadMessagesThroughput() {
        return -1;
    }

    @Override
    public double getWrittenMessagesThroughput() {
        return -1;
    }

    @Override
    public int getScheduledWriteMessages() {
        return -1;
    }

    @Override
    public long getScheduledWriteBytes() {
        return -1;
    }

    @Override
    public Object getCurrentWriteMessage() {
        return null;
    }

    @Override
    public WriteRequest getCurrentWriteRequest() {
        return null;
    }

    @Override
    public long getCreationTime() {
        return -1;
    }

    @Override
    public long getLastIoTime() {
        return -1;
    }

    @Override
    public long getLastReadTime() {
        return -1;
    }

    @Override
    public long getLastWriteTime() {
        return -1;
    }

    @Override
    public boolean isIdle(IdleStatus is) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isReaderIdle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isWriterIdle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBothIdle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIdleCount(IdleStatus is) {
        return -1;
    }

    @Override
    public int getReaderIdleCount() {
        return -1;
    }

    @Override
    public int getWriterIdleCount() {
        return -1;
    }

    @Override
    public int getBothIdleCount() {
        return -1;
    }

    @Override
    public long getLastIdleTime(IdleStatus is) {
        return -1;
    }

    @Override
    public long getLastReaderIdleTime() {
        return -1;
    }

    @Override
    public long getLastWriterIdleTime() {
        return -1;
    }

    @Override
    public long getLastBothIdleTime() {
        return -1;
    }

    @Deprecated
    @Override
    public CloseFuture close() {
        return null;
    }

    @Deprecated
    @Override
    public Object getAttachment() {
        return null;
    }

    @Deprecated
    @Override
    public Object setAttachment(Object o) {
        return null;
    }

    @Override
    public boolean isSecured() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
