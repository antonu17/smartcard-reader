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

import static com.google.common.collect.Sets.newHashSet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.swing.Timer;

import com.google.common.base.Objects;
import com.google.common.collect.ListMultimap;

/**
 * 
 * {@link AutomatedReader} can continuously perform certain {@link CardTask} by
 * a time interval.
 * 
 */
@SuppressWarnings("restriction")
public final class AutomatedReader {

  private final List<CommandAPDU> commands;
  private final CardTask task;
  private Timer timer;

  /**
   * Creates an {@link AutomatedReader}.
   * 
   * @param command
   *          a List of CommandAPDU
   * @param task
   *          a {@link CardTask}
   */
  public AutomatedReader(List<CommandAPDU> commands, CardTask task) {
    this.commands = commands;
    this.task = task;
  }

  /**
   * Starts to read Smartcards and performs the task continuously.
   * 
   * @param time
   *          in milliseconds
   */
  public synchronized void reading(int time) {
    final Set<CardResponse> lastResponses = newHashSet();
    timer = new Timer(time, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        ListMultimap<CardTerminal, CardResponse> responses =
            CardReader.getInstance().read(commands);
        if (lastResponses.addAll(responses.values())
            && !lastResponses.isEmpty()) {
          lastResponses.clear();
          lastResponses.addAll(responses.values());
          for (CardTerminal terminal : responses.keys()) {
            task.execute(terminal, responses.get(terminal));
          }
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
    return Objects.toStringHelper(getClass()).addValue(commands).toString();
  }

}
