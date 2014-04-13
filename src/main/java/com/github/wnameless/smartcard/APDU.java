/**
 *
 * @author Wei-Ming Wu
 *
 *
 * Copyright 2014 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.github.wnameless.smartcard;

import static com.google.common.base.Preconditions.checkArgument;
import static net.sf.rubycollect4j.RubyCollections.qr;
import static net.sf.rubycollect4j.RubyCollections.rs;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.smartcardio.CommandAPDU;

import net.sf.rubycollect4j.RubyString;

import com.github.wnameless.nullproof.annotation.RejectNull;
import com.google.common.primitives.Bytes;

/**
 * 
 * {@link APDU} is a helper class designed to make the creation of a CommandAPDU
 * easier.
 *
 */
@RejectNull
public final class APDU {

  private APDU() {}

  /**
   * Returns a builder of CommandAPDU.
   * 
   * @return a {@link APDUBuilder}
   */
  public static APDUBuilder builder() {
    return new APDUBuilder();
  }

  /**
   * 
   * {@link APDUBuilder} is designed to hold input data from user before a
   * CommandAPDU is created.
   *
   */
  @RejectNull
  public static class APDUBuilder {

    private final byte[] apdu = new byte[4];
    private byte[] lc = null;
    private byte[] data = null;
    private byte[] le = null;

    public APDUBuilder setCLA(byte claByte) {
      apdu[0] = claByte;
      return this;
    }

    public APDUBuilder setINS(byte insByte) {
      apdu[1] = insByte;
      return this;
    }

    public APDUBuilder setP1(byte p1Byte) {
      apdu[2] = p1Byte;
      return this;
    }

    public APDUBuilder setP2(byte p2Byte) {
      apdu[3] = p2Byte;
      return this;
    }

    private APDUBuilder setLc(int lcLength) {
      lc = lengthOfData(lcLength);
      return this;
    }

    private byte[] lengthOfData(int length) {
      byte[] lenBytes;
      if (length < 255) {
        lenBytes = new byte[] { (byte) length };
      } else {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.position(1);
        lenBytes = buffer.putShort((short) length).array();
      }
      return lenBytes;
    }

    private APDUBuilder clearLc() {
      lc = null;
      data = null;
      return this;
    }

    public APDUBuilder setData(byte... dataBytes) {
      checkArgument(dataBytes.length >= 1 && dataBytes.length <= 65535,
          "Data length is between 1..65535");
      setLc(dataBytes.length);
      data = dataBytes;
      return this;
    }

    public APDUBuilder setData(String hexString) {
      RubyString hexStr = rs(hexString).slice(qr("^[0-9A-Fa-f]+"));
      if (hexStr == null) {
        lc = null;
        data = null;
      } else {
        int suffixZero = hexStr.size() % 2;
        if (suffixZero != 0)
          hexStr.concat("0");
        data = new BigInteger(hexStr.toS(), 16).toByteArray();
        if (data.length < hexStr.length() / 2) {
          int prefixBytesNum = hexStr.length() / 2 - data.length;
          byte[] prefixBytes = new byte[prefixBytesNum];
          data = Bytes.concat(prefixBytes, data);
        } else if (data.length > hexStr.length() / 2) {
          data =
              Arrays.copyOfRange(data, data.length - hexStr.length() / 2,
                  data.length);
        }
        setLc(data.length);
      }
      return this;
    }

    public APDUBuilder clearData() {
      clearLc();
      return this;
    }

    public APDUBuilder setLe(int leLength) {
      checkArgument(leLength >= 1 && leLength <= 65535,
          "Le is between 1..65535");
      le = lengthOfData(leLength);
      return this;
    }

    public APDUBuilder clearLe() {
      le = null;
      return this;
    }

    /**
     * Returns a CommandAPDU by user given data.
     * 
     * @return a CommandAPDU
     */
    public CommandAPDU build() {
      byte[] finalApdu = apdu;
      if (lc != null)
        finalApdu = Bytes.concat(apdu, lc, data);
      if (le != null)
        finalApdu = Bytes.concat(finalApdu, le);
      return new CommandAPDU(finalApdu);
    }

  }

}
