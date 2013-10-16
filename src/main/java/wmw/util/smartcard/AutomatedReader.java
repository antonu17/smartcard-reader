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
package wmw.util.smartcard;

import static com.google.common.collect.Sets.newHashSet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.smartcardio.CommandAPDU;
import javax.swing.Timer;

import com.google.common.base.Objects;

@SuppressWarnings("restriction")
public final class AutomatedReader {

  private final CommandAPDU command;
  private final CardTask task;
  private Timer timer;

  public AutomatedReader(CommandAPDU command, CardTask task) {
    if (command == null || task == null)
      throw new NullPointerException();

    this.command = command;
    this.task = task;
  }

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

  public synchronized void stop() {
    timer.stop();
  }

  public String toString() {
    return Objects.toStringHelper(this.getClass()).add("Command", command)
        .add("Task", task).toString();
  }

}
