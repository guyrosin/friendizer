/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.teamagly.friendizer;

import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.Locator;

public class MessageLocator extends Locator<ClientMessage, Void> {
  
  @Override
  public ClientMessage create(Class<? extends ClientMessage> clazz) {
    return new ClientMessage(RequestFactoryServlet.getThreadLocalRequest().getSession().getServletContext());
  }

  @Override
  public ClientMessage find(Class<? extends ClientMessage> clazz, Void id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<ClientMessage> getDomainType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void getId(ClientMessage domainObject) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<Void> getIdType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getVersion(ClientMessage domainObject) {
    throw new UnsupportedOperationException();
  }
}
