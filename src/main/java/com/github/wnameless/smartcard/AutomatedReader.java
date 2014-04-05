/**
 *
 * @author Wei-Ming Wu
 *
 *
 * Copyright 2013 Wei-Ming Wu
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.smartcardio.CommandAPDU;
import javax.swing.Timer;

import com.google.common.base.Objects;

/**
 * 
 * AutomatedReader can continuously perform certain CardTask by a time interval.
 * 
 */
@SuppressWarnings("restriction")
public final class AutomatedReader {

  private final CommandAPDU command;
  private final CardTask task;
  private Timer timer;

  /**
   * Creates an AutomatedReader.
   * 
   * @param command
   *          a CommandAPDU
   * @param task
   *          a CardTask
   */
  public AutomatedReader(CommandAPDU command, CardTask task) {
    this.command = checkNotNull(command);
    this.task = checkNotNull(task);
  }

  /**
   * Starts to read smart cards and performs the task continuously.
   * 
   * @param time
   *          in milliseconds
   */
  public synchronized void reading(int time) {
    final Set<CardResponse> lastResponses = newHashSet();
    timer = new Timer(time, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        Set<CardResponse> responses = CardReader.read(command);
        if (lastResponses.addAll(responses) && !lastResponses.isEmpty()) {
          lastResponses.clear();
          lastResponses.addAll(responses);
          task.execute(responses);
        }
      }

    });
    timer.start();
  }

  /**
   * Stops the reading of smart cards.
   */
  public synchronized void stop() {
    timer.stop();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("Command", command)
        .add("Task", task).toString();
  }

}
