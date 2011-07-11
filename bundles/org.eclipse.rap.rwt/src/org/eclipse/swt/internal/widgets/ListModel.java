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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.SerializableCompatibility;


public final class ListModel implements SerializableCompatibility {

  private static final int[] EMPTY_SELECTION = new int[ 0 ];

  private final boolean single;
  private final java.util.List<String> items;
  private int[] selection;

  public ListModel( boolean single ) {
    this.single = single;
    items = new ArrayList<String>();
    selection = EMPTY_SELECTION;
  }

  ///////////////////////////////
  // Methods to get/set selection

  public int getSelectionIndex() {
    int result = -1;
    if( selection.length > 0 ) {
      result = selection[ 0 ];
    }
    return result;
  }

  public int[] getSelectionIndices() {
    int[] result = new int[ selection.length ];
    System.arraycopy( selection, 0, result, 0, selection.length );
    return result;
  }
  
  public int getSelectionCount() {
    return selection.length;
  }

  public void setSelection( int selection ) {
    deselectAll();
    if( selection >= 0 && selection <= getItemCount() - 1 ) {
      this.selection = new int[]{ selection };
    }
  }

  public void setSelection( int[] selection ) {
    if( selection == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    deselectAll();
    int length = selection.length;
    if( single ) {
      int end = getItemCount() - 1;
      if( length == 1 && selection[ 0 ] >= 0 && selection[ 0 ] <= end ) {
        this.selection = new int[]{ selection[ 0 ] };
      }
    } else {
      int end = getItemCount() - 1;
      int newLength = 0;
      for( int i = 0; i < length; i++ ) {
        if( selection[ i ] >= 0 && selection[ i ] <= end ) {
          newLength++;
        }
      }
      this.selection = new int[ newLength ];
      int pos = 0;
      for( int i = 0; i < length; i++ ) {
        if( selection[ i ] >= 0 && selection[ i ] <= end ) {
          this.selection[ pos ] = selection[ i ];
          pos++;
        }
      }
    }
  }
  
  public void setSelection( int start, int end ) {
    deselectAll();
    if( end >= 0 && start <= end && start <= getItemCount() - 1 ) {
      if( single ) {
        if( start == end ) {
          this.selection = new int[]{ start };
        }
      } else {
        int first = Math.max( 0, start );
        int last = Math.min( end, getItemCount() - 1 );
        this.selection = new int[ last - first + 1 ];
        int current = first;
        for( int i = 0; i < this.selection.length; i++ ) {
          this.selection[ i ] = current;
          current++;
        }
      }
    }
  }
  
  public void setSelection( String[] selection ) {
    if( selection == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    deselectAll();
    int length = selection.length;
    if( ( single && length == 1 ) || ( !single && length > 0 ) ) {
      int newLength = 0;
      for( int i = 0; i < length; i++ ) {
        if( selection[ i ] != null && indexOf( selection[ i ] ) != -1 ) {
          newLength++;
        }
      }
      this.selection = new int[ newLength ];
      int pos = 0;
      for( int i = 0; i < length; i++ ) {
        if( selection[ i ] != null && indexOf( selection[ i ] ) != -1 ) {
          this.selection[ pos ] = indexOf( selection[ i ] );
          pos++;
        }
      }
    }
  }

  public void addSelection( int index ) {
    if( index >= 0 && index < getItemCount() ) {
      boolean exists = false;
      for( int i = 0; i < selection.length; i++ ) {
        if( selection[ i ] == index ) {
          exists = true;
        }
      }
      if( !exists ) {
        int newLength = selection.length + 1;
        int[] newSelection = new int[ newLength ];
        System.arraycopy( selection, 0, newSelection, 0, selection.length );
        newSelection[ newLength - 1 ] = index;
        this.selection = newSelection;
      }
    }
  }

  public void selectAll() {
    if( !single ) {
      selection = new int[ items.size() ];
      for( int i = 0; i < selection.length; i++ ) {
        selection[ i ] = i;
      }
    }
  }

  public void deselectAll() {
    this.selection = EMPTY_SELECTION;
  }
  
  ////////////////////////////////
  // Methods to maintain the items
  
  public void add( String string ) {
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    items.add( string );
  }

  public void add( String string, int index ) {
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( index != getItemCount() ) {
      checkIndex( index );
    }
    items.add( index, string );
  }

  public void remove( int index ) {
    checkIndex( index );
    items.remove( index );
    removeFromSelection( index );
  }

  public void remove( int start, int end ) {
    checkIndex( start );
    checkIndex( end );
    for( int i = end; i >= start; i-- ) {
      remove( i );
    }
  }
  
  public void remove( int[] indices ) {
    if( indices == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( indices.length > 0 ) {
      int[] newIndices = new int[ indices.length ];
      System.arraycopy( indices, 0, newIndices, 0, indices.length );
      Arrays.sort( newIndices );
      checkIndex( newIndices[ 0 ] );
      checkIndex( newIndices[ newIndices.length - 1 ] );
      for( int i = newIndices.length - 1; i >= 0; i-- ) {
        remove( newIndices[ i ] );
      }
    }
  }

  public void remove( String string ) {
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    int index = indexOf( string );
    checkIndex( index );
    remove( index );
  }

  public void removeAll() {
    items.clear();
    deselectAll();
  }

  public void setItem( int index, String string ) {
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    checkIndex( index );
    items.set( index, string );
  }

  public void setItems( String[] items ) {
    if( items == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    } 
    for( int i = 0; i < items.length; i++ ) {
      if( items[ i ] == null ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
    }
    this.items.clear();
    this.items.addAll( Arrays.asList( items ) );
    deselectAll();
  }

  public String getItem( int index ) {
    checkIndex( index );
    return items.get( index );
  }

  public int getItemCount() {
    return items.size();
  }

  public String[] getItems() {
    return items.toArray( new String[ items.size() ] );
  }

  //////////////////
  // Helping methods 
  
  /* If the given index is contained in the selection, it will be removed. */
  private void removeFromSelection( int index ) {
    boolean found = false;
    for( int i = 0; !found && i < selection.length; i++ ) {
      if( index == selection[ i ] ) {
        int length = selection.length;
        int[] newSelection = new int[ selection.length - 1 ];
        System.arraycopy( selection, 0, newSelection, 0, i );
        if( i < length - 1 ) {
          System.arraycopy( selection, i + 1, newSelection, i, length - i - 1 );
        }
        for( int j = 0; j < newSelection.length; j++ ) {
          if( newSelection[ j ] > index ) {
            newSelection[ j ] = newSelection[ j ] - 1;
          }
        }
        selection = newSelection;
        found = true;
      }
    }
  }
  
  private int indexOf( String string ) {
    return items.indexOf( string );
  }

  private void checkIndex( int index ) {
    if( index < 0 || index >= getItemCount() ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
  }
}
