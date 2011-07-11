/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.internal.lifecycle.DisposedWidgets;
import org.eclipse.rwt.internal.lifecycle.IRenderRunnable;
import org.eclipse.rwt.lifecycle.IWidgetAdapter;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Widget;

public final class WidgetAdapter implements IWidgetAdapter, SerializableCompatibility {

  private final String id;
  private boolean initialized;
  private /*final*/ transient Map<String,Object> preservedValues;
  private String jsParent;
  private IRenderRunnable renderRunnable;
  private String variant;

  public WidgetAdapter() {
    this( IdGenerator.getInstance().newId() );
  }

  public WidgetAdapter( String id ) {
    this.id = id;
    initialize();
  }

  private void initialize() {
    preservedValues = new HashMap<String,Object>();
  }

  public String getId() {
    return id;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized( boolean initialized ) {
    this.initialized = initialized;
  }

  public void preserve( String propertyName, Object value ) {
    preservedValues.put( propertyName, value );
  }

  public Object getPreserved( String propertyName ) {
    return preservedValues.get( propertyName );
  }

  public void clearPreserved() {
    preservedValues.clear();
  }

  public String getJSParent() {
    return jsParent;
  }

  public void setJSParent( String jsParent ) {
    this.jsParent = jsParent;
  }

  public void setRenderRunnable( IRenderRunnable renderRunnable ) {
    if( this.renderRunnable != null ) {
      throw new IllegalStateException( "A renderRunnable was already set." );
    }
    this.renderRunnable = renderRunnable;
  }

  public IRenderRunnable getRenderRunnable() {
    return renderRunnable;
  }

  public void clearRenderRunnable() {
    renderRunnable = null;
  }

  public String getCachedVariant() {
    return variant;
  }

  public void setCachedVariant( String variant ) {
    this.variant = variant;
  }

  public void markDisposed( Widget widget ) {
    if( initialized ) {
      DisposedWidgets.add( widget );
    }
  }
  
  private Object readResolve() {
    initialize();
    return this;
  }
}
