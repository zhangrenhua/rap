/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.widgets.*;


/**
 * Instances of this class represent a selectable user interface object that
 * represents a hierarchy of tree items in a tree widget.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */
public class TreeItem extends Item {

  private static final int EMPTY_PREFERED_WIDTH_BUFFER = -1;

  private final class TreeItemAdapter
    implements ITreeItemAdapter, IWidgetFontAdapter, IWidgetColorAdapter
  {

    public boolean isParentDisposed() {
      return TreeItem.this.parent.isDisposed();
    }

    public Color getUserBackgound() {
      return TreeItem.this.background;
    }

    public Color getUserForegound() {
      return TreeItem.this.foreground;
    }

    public Font getUserFont() {
      return TreeItem.this.font;
    }

    public Color[] getCellBackgrounds() {
      Color[] backgrounds = null;
      if( TreeItem.this.cellBackgrounds != null ) {
        backgrounds = TreeItem.this.cellBackgrounds.clone();
      }
      return backgrounds;
    }

    public Color[] getCellForegrounds() {
      Color[] foregrounds = null;
      if( TreeItem.this.cellForegrounds != null ) {
        foregrounds = TreeItem.this.cellForegrounds.clone();
      }
      return foregrounds;
    }

    public Font[] getCellFonts() {
      Font[] fonts = null;
      if( TreeItem.this.cellFonts != null ) {
        fonts = TreeItem.this.cellFonts.clone();
      }
      return fonts;
    }
  }

  private final class CompositeItemHolder implements IItemHolderAdapter {
    public void add( Item item ) {
      throw new UnsupportedOperationException();
    }
    public void insert( Item item, int index ) {
      throw new UnsupportedOperationException();
    }
    public void remove( Item item ) {
      throw new UnsupportedOperationException();
    }
    public Item[] getItems() {
      TreeItem[] items = getCreatedItems();
      Item[] result = new Item[ items.length ];
      System.arraycopy( items, 0, result, 0, items.length );
      return result;
    }
  }

  private final TreeItem parentItem;
  final Tree parent;
  private TreeItem[] items;
  int itemCount;
  private transient ITreeItemAdapter treeItemAdapter;
  int index;
  private Font font;
  private boolean expanded;
  private boolean checked;
  private Color background;
  private Color foreground;
  private boolean grayed;
  private String[] texts;
  private Image[] images;
  Color[] cellForegrounds, cellBackgrounds;
  Font[] cellFonts;
  int depth;
  private boolean cached;
  int flatIndex;

  // Note [fappel]: Yourkit analysis with the UI workbench testsuite showed an extensive
  //                appearance of preferred width calculations. Buffering the preferred width
  //                speeds up the suite on my machine to 1/4th of the time needed without buffering.
  private int preferredWidthBuffer;

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>) and a style value describing
   * its behavior and appearance. The item is added to the end of the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a tree control which will be the parent of the new instance
   *          (cannot be null)
   * @param style the style of control to construct
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( Tree parent, int style ) {
    this( parent, null, style, parent == null ? 0 : parent.getItemCount(), true );
  }

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>), a style value describing its
   * behavior and appearance, and the index at which to place it in the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a tree control which will be the parent of the new instance
   *          (cannot be null)
   * @param style the style of control to construct
   * @param index the zero-relative index to store the receiver in its parent
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the parent (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( Tree parent, int style, int index ) {
    this( parent, null, style, index, true );
  }

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>) and a style value describing
   * its behavior and appearance. The item is added to the end of the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parentItem a tree control which will be the parent of the new
   *          instance (cannot be null)
   * @param style the style of control to construct
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( TreeItem parentItem, int style ) {
    this( parentItem == null ? null : parentItem.parent,
          parentItem,
          style,
          parentItem == null ? 0 : parentItem.getItemCount( false ), true );
  }

  /**
   * Constructs a new instance of this class given its parent (which must be a
   * <code>Tree</code> or a <code>TreeItem</code>), a style value describing its
   * behavior and appearance, and the index at which to place it in the items
   * maintained by its parent.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parentItem a tree control which will be the parent of the new
   *          instance (cannot be null)
   * @param style the style of control to construct
   * @param index the zero-relative index to store the receiver in its parent
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the parent (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public TreeItem( TreeItem parentItem, int style, int index ) {
    this( parentItem == null ? null : parentItem.parent, parentItem, style, index, true );
  }

  TreeItem( Tree parent, TreeItem parentItem, int style, int index, boolean create ) {
    super( parent, style );
    this.parent = parent;
    this.parentItem = parentItem;
    this.index = index;
    int columnCount = parent.columnHolder.size();
    this.texts = new String[ columnCount ];
    this.images = new Image[ columnCount ];
    if( parentItem != null ) {
      this.depth = parentItem.depth + 1;
    }
    clearPreferredWidthBuffer();
    setEmpty();
    if( create ) {
      int numberOfItems;
      if( parentItem != null ) {
        numberOfItems = parentItem.getItemCount( false );
      } else {
        // If there is no parent item, get the next index of the tree
        numberOfItems = parent.getItemCount();
      }
      // check range
      if( index < 0 || index > numberOfItems ) {
        error( SWT.ERROR_INVALID_RANGE );
      }
      if( parentItem != null ) {
        parentItem.createItem( this, index );
      } else {
        parent.createItem( this, index );
      }
      parent.updateFlatIndices();
      parent.updateScrollBars();
    }
  }
  
  private void setEmpty() {
    items = new TreeItem[ 4 ];
  }

  private void createItem( TreeItem item, int index ) {
    if( itemCount == items.length ) {
      /*
       * Grow the array faster when redraw is off or the table is not visible.
       * When the table is painted, the items array is resized to be smaller to
       * reduce memory usage.
       */
      boolean small = /* drawCount == 0 && */isVisible();
      int length = small ? items.length + 4 : Math.max( 4, items.length * 3 / 2 );
      TreeItem[] newItems = new TreeItem[ length ];
      System.arraycopy( items, 0, newItems, 0, items.length );
      items = newItems;
    }
    System.arraycopy( items, index, items, index + 1, itemCount - index );
    items[ index ] = item;
    itemCount++;
    adjustItemIndices( index );
  }

  private void destroyItem( TreeItem item, int index ) {
    itemCount--;
    if( itemCount == 0 ) {
      setEmpty();
    } else {
      System.arraycopy( items, index + 1, items, index, itemCount - index );
      items[ itemCount ] = null;
    }
    adjustItemIndices( index );
  }

  private void adjustItemIndices( int start ) {
    for( int i = start; i < itemCount; i++ ) {
      if( items[ i ] != null ) {
        items[ i ].index = i;
      }
    }
  }

  public Object getAdapter( Class adapter ) {
    Object result;
    if( adapter == IItemHolderAdapter.class ) {
      result = new CompositeItemHolder();
    } else if(    adapter == IWidgetFontAdapter.class 
               || adapter == IWidgetColorAdapter.class
               || adapter == ITreeItemAdapter.class )
    {
      if( treeItemAdapter == null ) {
        treeItemAdapter = new TreeItemAdapter();
      }
      result = treeItemAdapter;
    } else {
      result = super.getAdapter( adapter );
    }
    return result;
  }

  //////////////////////////
  // Parent/child relations
  
  /**
   * Returns the receiver's parent, which must be a <code>Tree</code>.
   *
   * @return the receiver's parent
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Tree getParent() {
    checkWidget();
    return parent;
  }

  /**
   * Returns the receiver's parent item, which must be a <code>TreeItem</code>
   * or null when the receiver is a root.
   *
   * @return the receiver's parent item
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public TreeItem getParentItem() {
    checkWidget();
    return parentItem;
  }

  /////////////////
  // Getter/Setter
  
  /**
   * Sets the expanded state of the receiver.
   * <p>
   *
   * @param expanded the new expanded state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setExpanded( boolean expanded ) {
    checkWidget();
    markCached();
    if( !expanded || getItemCount() > 0 ) {
      this.expanded = expanded;
      parent.updateFlatIndices();
      parent.updateScrollBars();
      parent.checkAllData();
    }
  }

  /**
   * Returns <code>true</code> if the receiver is expanded, and false otherwise.
   * <p>
   *
   * @return the expanded state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public boolean getExpanded() {
    checkWidget();
    return expanded;
  }

  /**
   * Returns a rectangle describing the receiver's size and location relative to
   * its parent.
   *
   * @return the receiver's bounding rectangle
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public Rectangle getBounds() {
    checkWidget();
    return getBounds( 0 );
  }

  /**
   * Returns a rectangle describing the receiver's size and location relative to
   * its parent at a column in the tree.
   *
   * @param columnIndex the index that specifies the column
   * @return the receiver's bounding column rectangle
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Rectangle getBounds( int columnIndex ) {
    checkWidget();
    return getBounds( columnIndex, true );
  }

  Rectangle getBounds( int columnIndex, boolean checkData ) {
    Rectangle result = new Rectangle( 0, 0, 0, 0 );
    if( isVisible() && isValidColumn( columnIndex ) ) {
      int left = parent.getVisualCellLeft( columnIndex, this );
      int width = parent.getVisualCellWidth( columnIndex, this, checkData );
      result = new Rectangle( left, getItemTop(), width, parent.getItemHeight() );
    }
    return result;
  }

  private boolean isValidColumn( int index ) {
    int columnCount = parent.getColumnCount();
    return ( columnCount == 0 && index == 0 ) || ( index >= 0 && index < columnCount );
  }

  private boolean isVisible() {
    return getParentItem() == null || getParentItem().getExpanded();
  }

  int getItemTop() {
    int headerHeight = parent.getHeaderHeight();
    int itemHeight = parent.getItemHeight();
    return headerHeight + ( flatIndex - parent.getTopIndex() ) * itemHeight;
  }

  /**
   * Returns the background color at the given column index in the receiver.
   *
   * @param columnIndex the column index
   * @return the background color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Color getBackground( int columnIndex ) {
    checkWidget();
    // if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return getBackground();
    }
    if( cellBackgrounds == null || cellBackgrounds[ columnIndex ] == null ) {
      return getBackground();
    }
    return cellBackgrounds[ columnIndex ];
  }

  /**
   * Returns the font that the receiver will use to paint textual information
   * for the specified cell in this item.
   *
   * @param columnIndex the column index
   * @return the receiver's font
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Font getFont( int columnIndex ) {
    checkWidget();
    return getFont( columnIndex, true );
  }

  Font getFont( int columnIndex, boolean checkData ) {
    // if (checkData && !parent.checkData (this, true)) error
    // (SWT.ERROR_WIDGET_DISPOSED);
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return getFont( checkData );
    }
    if( cellFonts == null || cellFonts[ columnIndex ] == null ) {
      return getFont( checkData );
    }
    if( checkData ) {
      materialize();
    }
    return cellFonts[ columnIndex ];
  }

  /**
   * Returns the foreground color at the given column index in the receiver.
   *
   * @param columnIndex the column index
   * @return the foreground color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Color getForeground( int columnIndex ) {
    checkWidget();
    // if (!parent.checkData (this, true)) error (SWT.ERROR_WIDGET_DISPOSED);
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return getForeground();
    }
    if( cellForegrounds == null || cellForegrounds[ columnIndex ] == null ) {
      return getForeground();
    }
    return cellForegrounds[ columnIndex ];
  }

  /**
   * Sets the background color at the given column index in the receiver to the
   * color specified by the argument, or to the default system color for the
   * item if the argument is null.
   *
   * @param columnIndex the column index
   * @param value the new color (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setBackground( int columnIndex, Color value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return;
    }
    if( cellBackgrounds == null ) {
      cellBackgrounds = new Color[ validColumnCount ];
    } else if( cellBackgrounds.length < validColumnCount ) {
      Color[] newCellBackgrounds = new Color[ validColumnCount ];
      System.arraycopy( cellBackgrounds, 0, newCellBackgrounds, 0, cellBackgrounds.length );
      cellBackgrounds = newCellBackgrounds;
    }
    if( cellBackgrounds[ columnIndex ] == value ) {
      return;
    }
    if( cellBackgrounds[ columnIndex ] != null
        && cellBackgrounds[ columnIndex ].equals( value ) )
    {
      return;
    }
    cellBackgrounds[ columnIndex ] = value;
    markCached();
  }

  /**
   * Sets the font that the receiver will use to paint textual information for
   * the specified cell in this item to the font specified by the argument, or
   * to the default font for that kind of control if the argument is null.
   *
   * @param columnIndex the column index
   * @param value the new font (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setFont( int columnIndex, Font value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return;
    }
    if( cellFonts == null ) {
      if( value == null ) {
        return;
      }
      cellFonts = new Font[ validColumnCount ];
    } else if( cellFonts.length < validColumnCount ) {
      Font[] newCellFonts = new Font[ validColumnCount ];
      System.arraycopy( cellFonts, 0, newCellFonts, 0, cellFonts.length );
      cellFonts = newCellFonts;
    }
    if( cellFonts[ columnIndex ] == value ) {
      return;
    }
    if( cellFonts[ columnIndex ] != null && cellFonts[ columnIndex ].equals( value ) ) {
      return;
    }
    cellFonts[ columnIndex ] = value;
    markCached();
  }

  /**
   * Sets the foreground color at the given column index in the receiver to the
   * color specified by the argument, or to the default system color for the
   * item if the argument is null.
   *
   * @param columnIndex the column index
   * @param value the new color (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setForeground( int columnIndex, Color value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return;
    }
    if( cellForegrounds == null ) {
      cellForegrounds = new Color[ validColumnCount ];
    } else if( cellForegrounds.length < validColumnCount ) {
      Color[] newCellForegrounds = new Color[ validColumnCount ];
      System.arraycopy( cellForegrounds, 0, newCellForegrounds, 0, cellForegrounds.length );
      cellForegrounds = newCellForegrounds;
    }
    if( cellForegrounds[ columnIndex ] == value ) {
      return;
    }
    if( cellForegrounds[ columnIndex ] != null && cellForegrounds[ columnIndex ].equals( value ) ) {
      return;
    }
    cellForegrounds[ columnIndex ] = value;
    if( parent.isVirtual() ) {
      cached = true;
    }
  }

  /**
   * Sets the font that the receiver will use to paint textual information for
   * this item to the font specified by the argument, or to the default font for
   * that kind of control if the argument is null.
   *
   * @param font the new font (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setFont( Font font ) {
    checkWidget();
    if( font != null && font.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    this.font = font;
    markCached();
  }

  /**
   * Returns the font that the receiver will use to paint textual information
   * for this item.
   *
   * @return the receiver's font
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Font getFont() {
    checkWidget();
    return getFont( true );
  }

  Font getFont( boolean checkData ) {
    if( checkData ) {
      materialize();
    }
    if( font != null ) {
      return font;
    }
    return parent.getFont();
  }

  /**
   * Sets the receiver's background color to the color specified by the
   * argument, or to the default system color for the item if the argument is
   * null.
   *
   * @param value the new color (or null)
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setBackground( Color value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( background == value ) {
      return;
    }
    if( background != null && background.equals( value ) ) {
      return;
    }
    background = value;
    markCached();
  }

  /**
   * Returns the receiver's background color.
   *
   * @return the background color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Color getBackground() {
    checkWidget();
    if( isDisposed() ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    materialize();
    if( background != null ) {
      return background;
    }
    return parent.getBackground();
  }

  /**
   * Returns the foreground color that the receiver will use to draw.
   *
   * @return the receiver's foreground color
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Color getForeground() {
    checkWidget();
    if( isDisposed() ) {
      error( SWT.ERROR_WIDGET_DISPOSED );
    }
    materialize();
    if( foreground != null ) {
      return foreground;
    }
    return parent.getForeground();
  }

  /**
   * Sets the receiver's foreground color to the color specified by the
   * argument, or to the default system color for the item if the argument is
   * null.
   *
   * @param value the new color (or null)
   * @since 1.0
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setForeground( Color value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( foreground != value ) {
      if( foreground == null || !foreground.equals( value ) ) {
        foreground = value;
        markCached();
      }
    }
  }

  /**
   * Sets the checked state of the receiver.
   * <p>
   *
   * @param checked the new checked state
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setChecked( boolean checked ) {
    checkWidget();
    if( ( parent.getStyle() & SWT.CHECK ) != 0 ) {
      if( checked != this.checked ) {
        this.checked = checked;
        markCached();
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver is checked, and false otherwise.
   * When the parent does not have the <code>CHECK style, return false.
   * <p>
   *
   * @return the checked state
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public boolean getChecked() {
    checkWidget();
    materialize();
    return checked;
  }

  /**
   * Sets the grayed state of the checkbox for this item. This state change only
   * applies if the Tree was created with the SWT.CHECK style.
   *
   * @param value the new grayed state of the checkbox
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setGrayed( boolean value ) {
    checkWidget();
    if( ( parent.getStyle() & SWT.CHECK ) != 0 ) {
      if( grayed != value ) {
        grayed = value;
        markCached();
      }
    }
  }

  /**
   * Returns <code>true</code> if the receiver is grayed, and false otherwise.
   * When the parent does not have the <code>CHECK style, return false.
   * <p>
   *
   * @return the grayed state of the checkbox
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public boolean getGrayed() {
    checkWidget();
    materialize();
    // error( SWT.ERROR_WIDGET_DISPOSED );
    return grayed;
  }

  /**
   * Returns the text stored at the given column index in the receiver, or empty
   * string if the text has not been set.
   *
   * @param columnIndex the column index
   * @return the text stored at the given column index in the receiver
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public String getText( int columnIndex ) {
    checkWidget();
    return getText( columnIndex, true );
  }

  /**
   * Returns the receiver's text, which will be an empty string if it has never
   * been set.
   *
   * @return the receiver's text
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public String getText() {
    checkWidget();
    materialize();
    return super.getText();
  }

  String getText( int columnIndex, boolean checkData ) {
    if( checkData && !isCached() ) {
      parent.checkData( this, this.index );
    }
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return ""; //$NON-NLS-1$
    }
    if( columnIndex == 0 ) {
      return super.getText(); /* super is intentional here */
    }
    if( texts[ columnIndex ] == null ) {
      return ""; //$NON-NLS-1$
    }
    return texts[ columnIndex ];
  }

  /**
   * Returns a rectangle describing the size and location
   * relative to its parent of the text at a column in the
   * tree.
   *
   * @param index the index that specifies the column
   * @return the receiver's bounding text rectangle
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public Rectangle getTextBounds( int index ) {
    checkWidget();
    Rectangle result = new Rectangle( 0, 0, 0, 0 );
    if( isVisible() && isValidColumn( index ) ) {
      result.x = parent.getVisualTextLeft( index, this );
      result.y = getItemTop();
      result.width = parent.getVisualTextWidth( index, this );
      result.height = parent.getItemHeight();
    }
    return result;
  }


  /**
   * Sets the text for multiple columns in the tree.
   *
   * @param value the array of new strings
   * @exception IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the
   *              text is null</li> </ul>
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setText( String[] value ) {
    checkWidget();
    if( value == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 0; i < value.length; i++ ) {
      if( value[ i ] != null ) {
        setText( i, value[ i ] );
      }
    }
  }

  /**
   * Sets the receiver's text at a column
   *
   * @param columnIndex the column index
   * @param value the new text
   * @exception IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the
   *              text is null</li> </ul>
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setText( int columnIndex, String value ) {
    checkWidget();
    if( value == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return;
    }
    if( value.equals( getText( columnIndex, false ) ) ) {
      return;
    }
    if( columnIndex == 0 ) {
      super.setText( value );
    } else {
      texts[ columnIndex ] = value;
    }
    clearPreferredWidthBuffer();
    if( parent.getColumnCount() == 0 ) {
      parent.updateScrollBars();
    }
    markCached();
  }

  /**
   * Sets the receiver's text.
   *
   * @param text the new text
   * @exception IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the
   *              text is null</li> </ul>
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setText( String text ) {
    checkWidget();
    setText( 0, text );
  }

  /**
   * Returns the image stored at the given column index in the receiver, or null
   * if the image has not been set or if the column does not exist.
   *
   * @param columnIndex the column index
   * @return the image stored at the given column index in the receiver
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public Image getImage( int columnIndex ) {
    checkWidget();
    return getImage( columnIndex, true );
  }

  /**
   * Returns the receiver's image if it has one, or null
   * if it does not.
   *
   * @return the receiver's image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.4
   */
  public Image getImage() {
    return getImage( 0 );
  }

  /**
   * Returns a rectangle describing the size and location relative to its parent
   * of an image at a column in the tree.
   *
   * @param index the index that specifies the column
   * @return the receiver's bounding image rectangle
   * @exception SWTException <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver
   *              has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
   *              not called from the thread that created the receiver</li>
   *              </ul>
   */
  public Rectangle getImageBounds( int columnIndex ) {
    checkWidget();
    // parent.checkData( this, parent.indexOf( this ) );
    Rectangle result = null;
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( ( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      result = new Rectangle( 0, 0, 0, 0 );
      Point size = parent.getItemImageSize( columnIndex );
      result.width = size.x;
      result.height = size.y;
      result.x = parent.getVisualCellLeft( columnIndex, this );
      result.x += parent.getCellPadding().x;
      // SWT behavior on windows gives the correct y value
      // On Gtk the y value is always the same (eg. 1)
      // we emulate the default windows behavior here
      result.y = getItemTop();
    } else {
      result = new Rectangle( 0, 0, 0, 0 );
    }
    return result;
  }

  Image getImage( int columnIndex, boolean checkData ) {
    // if( checkData ) parent.checkData( this, this.index );
    int validColumnCount = Math.max( 1, parent.columnHolder.size() );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return null;
    }
    if( checkData ) {
      materialize();
    }
    if( columnIndex == 0 ) {
      return super.getImage(); /* super is intentional here */
    }
    return images[ columnIndex ];
  }

  /*
   * Returns the receiver's ideal width for the specified columnIndex.
   */
  int getPreferredWidth( int columnIndex, boolean checkData ) {
    return parent.getPreferredCellWidth( this, columnIndex, checkData );
  }

  void clear() {
    checked = false;
    grayed = false;
    texts = null;
    images = null;
    foreground = background = null;
    cellForegrounds = cellBackgrounds = null;
    font = null;
    cellFonts = null;
    setText( "" );
    setImage( ( Image )null );
    int columnCount = parent.columnHolder.size();
    if( columnCount > 0 ) {
      // displayTexts = new String[ columnCount ];
      if( columnCount > 1 ) {
        texts = new String[ columnCount ];
        images = new Image[ columnCount ];
      }
    }
    clearCached();
    parent.updateScrollBars();
  }

  /**
   * Clears the item at the given zero-relative index in the receiver. The text,
   * icon and other attributes of the item are set to the default value. If the
   * tree was created with the <code>SWT.VIRTUAL</code> style, these attributes
   * are requested again as needed.
   *
   * @param index the index of the item to clear
   * @param recursive <code>true</code> if all child items of the indexed item
   *          should be cleared recursively, and <code>false</code> otherwise
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the list minus 1 (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   * @since 1.0
   */
  public void clear( int index, boolean recursive ) {
    checkWidget();
    if( index < 0 || index >= itemCount ) {
      error( SWT.ERROR_INVALID_RANGE );
    }
    TreeItem item = items[ index ];    
    if( item != null ) {
      item.clear();
      if( recursive ) {
        item.clearAll( true, false );
      }
    }
  }

  /*
   * Updates internal structures in the receiver and its child items to handle
   * the creation of a new column.
   */
  void addColumn( TreeColumn column ) {
    int index = column.getIndex();
    int columnCount = parent.columnHolder.size();
    if( columnCount > 1 ) {
      if( columnCount == 2 ) {
        texts = new String[ 2 ];
      } else {
        String[] newTexts = new String[ columnCount ];
        System.arraycopy( texts, 0, newTexts, 0, index );
        System.arraycopy( texts, index, newTexts, index + 1, columnCount - index - 1 );
        texts = newTexts;
      }
      if( index == 0 ) {
        texts[ 1 ] = text;
        text = ""; //$NON-NLS-1$
      }
      if( columnCount == 2 ) {
        images = new Image[ 2 ];
      } else {
        Image[] newImages = new Image[ columnCount ];
        System.arraycopy( images, 0, newImages, 0, index );
        System.arraycopy( images, index, newImages, index + 1, columnCount - index - 1 );
        images = newImages;
      }
      if( index == 0 ) {
        images[ 1 ] = image;
        image = null;
      }
    }
    /* notify all child items as well */
    for( int i = 0; i < itemCount; i++ ) {
      TreeItem item = items[ i ];
      if( item != null ) {
        item.addColumn( column );
      }
    }
  }

  public void setImage( Image image ) {
    checkWidget();
    setImage( 0, image );
  }

  /**
   * Sets the receiver's image at a column.
   *
   * @param columnIndex the column index
   * @param value the new image
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_ARGUMENT - if the image has been disposed
   *              </li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setImage( int columnIndex, Image value ) {
    checkWidget();
    if( value != null && value.isDisposed() ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    TreeColumn[] columns = parent.columnHolder.getItems();
    int validColumnCount = Math.max( 1, columns.length );
    if( !( 0 <= columnIndex && columnIndex < validColumnCount ) ) {
      return;
    }
    Image image = getImage( columnIndex, false );
    if( value == image ) {
      return;
    }
    if( value != null && value.equals( image ) ) {
      return;
    }
    parent.updateColumnImageCount( columnIndex, image, value );
    parent.updateItemImageSize( value );
    if( columnIndex == 0 ) {
      super.setImage( value );
    } else {
      images[ columnIndex ] = value;
    }
    markCached();
    clearPreferredWidthBuffer();
    if( parent.getColumnCount() == 0 ) {
      parent.updateScrollBars();
    }
  }

  /**
   * Sets the image for multiple columns in the tree.
   *
   * @param value the array of new images
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the array of images is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if one of the images has been
   *              disposed</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setImage( Image[] value ) {
    checkWidget();
    if( value == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 0; i < value.length; i++ ) {
      if( value[ i ] != null && value[ i ].isDisposed() ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
    }
    for( int i = 0; i < value.length; i++ ) {
      if( value[ i ] != null ) {
        setImage( i, value[ i ] );
      }
    }
  }

  /**
   * Clears all the items in the receiver. The text, icon and other attributes
   * of the items are set to their default values. If the tree was created with
   * the <code>SWT.VIRTUAL</code> style, these attributes are requested again as
   * needed.
   *
   * @param recursive <code>true</code> if all child items should be cleared
   *          recursively, and <code>false</code> otherwise
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @see SWT#VIRTUAL
   * @see SWT#SetData
   * @since 1.0
   */
  public void clearAll( boolean recursive ) {
    clearAll( recursive, true );
  }

  void clearAll( boolean recursive, boolean doVisualUpdate ) {
    checkWidget();
    for( int i = 0; i < itemCount; i++ ) {
      TreeItem item = items[ i ];
      if( item != null ) {
        item.clear();
        if( recursive ) {
          item.clearAll( true, false );
        }
        parent.checkData( item, item.index );
      }
    }
  }

  ////////////////////////////////////////
  // Methods to maintain (sub-) TreeItems
  
  /**
   * Returns a (possibly empty) array of <code>TreeItem</code>s which are the
   * direct item children of the receiver.
   * <p>
   * Note: This is not the actual structure used by the receiver to maintain its
   * list of items, so modifying the array will not affect the receiver.
   * </p>
   *
   * @return the receiver's items
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public TreeItem[] getItems() {
    checkWidget();
    TreeItem[] result = new TreeItem[ itemCount ];
    if( parent.isVirtual() ) {
      for( int i = 0; i < itemCount; i++ ) {
        result[ i ] = _getItem( i );
      }
    } else {
      System.arraycopy( items, 0, result, 0, itemCount );
    }
    return result;
  }
  
  private TreeItem _getItem( int index ) {
    if( parent.isVirtual() && items[ index ] == null ) {
      items[ index ] = new TreeItem( parent, this, SWT.NONE, index, false );
    }
    return items[ index ];
  }

  private TreeItem[] getCreatedItems() {
    TreeItem[] result;
    if( parent.isVirtual() ) {
      int count = 0;
      for( int i = 0; i < itemCount; i++ ) {
        if( items[ i ] != null ) {
          count++;
        }
      }
      result = new TreeItem[ count ];
      count = 0;
      for( int i = 0; i < itemCount; i++ ) {
        if( items[ i ] != null ) {
          result[ count ] = items[ i ];
          count++;
        }
      }
    } else {
      result = new TreeItem[ itemCount ];
      System.arraycopy( items, 0, result, 0, itemCount );
    }
    return result;
  }

  /**
   * Returns the item at the given, zero-relative index in the receiver. Throws
   * an exception if the index is out of range.
   *
   * @param index the index of the item to return
   * @return the item at the given index
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_INVALID_RANGE - if the index is not between 0 and
   *              the number of elements in the list minus 1 (inclusive)</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public TreeItem getItem( int index ) {
    checkWidget();
    if( index < 0 || index >= itemCount ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    return _getItem( index );
  }

  /**
   * Returns the number of items contained in the receiver that are direct item
   * children of the receiver.
   *
   * @return the number of items
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public int getItemCount() {
    return getItemCount( true );
  }

  int getItemCount( boolean checkData ) {
    checkWidget();
    if( checkData ) {
      materialize();
    }
    return itemCount;
  }

  /**
   * Searches the receiver's list starting at the first item (index 0) until an
   * item is found that is equal to the argument, and returns the index of that
   * item. If no item is found, returns -1.
   *
   * @param item the search item
   * @return the index of the item
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the tool item is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the tool item has been
   *              disposed</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public int indexOf( TreeItem item ) {
    checkWidget();
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( item.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return item.parentItem == this ? item.index : -1;
  }

  /**
   * Removes all of the items from the receiver.
   * <p>
   *
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void removeAll() {
    checkWidget();
    TreeItem[] items = getItems();
    for( int i = 0; i < items.length; i++ ) {
      items[ i ].dispose();
    }
  }

  /**
   * Sets the number of child items contained in the receiver.
   *
   * @param count the number of items
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
   *              </li> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.0
   */
  public void setItemCount( int count ) {
    checkWidget();
    int oldItemCount = this.getItemCount( false );
    int newItemCount = Math.max( 0, count );
    if( newItemCount != oldItemCount ) {
//      TreeItem[] items = this.getItems();
      int index = oldItemCount - 1;
      while( index >= newItemCount ) {
        TreeItem item = items[ index ];
        if( item != null && !item.isDisposed() ) {
          item.dispose();
        }
        index--;
      }
      int length = Math.max( 4, ( newItemCount + 3 ) / 4 * 4 );
      TreeItem[] newItems = new TreeItem[ length ];
      System.arraycopy( items, 0, newItems, 0, Math.min( newItemCount, itemCount ) );
      items = newItems;
      if( !parent.isVirtual() ) {
        for( int i = oldItemCount; i < newItemCount; i++ ) {
          new TreeItem( this, SWT.NONE, i );
        }
      }
      itemCount = newItemCount;
      parent.updateScrollBars();
    }
  }

  /////////////////////////////////
  // Methods to dispose of the item
  
  final void releaseChildren() {
    TreeItem[] items = getItems();
    for( int i = 0; i < items.length; i++ ) {
      items[ i ].dispose();
    }
  }

  final void releaseParent() {
    if( parentItem != null ) {
      parentItem.destroyItem( this, index );
    } else {
      parent.destroyItem( this, index );
    }
    if( !parent.isInDispose() ) {
      parent.removeFromSelection( this );
      parent.updateScrollBars();
    }
    super.releaseParent();
  }

  //////////////////
  // helping methods

  void clearPreferredWidthBuffer() {
    preferredWidthBuffer = EMPTY_PREFERED_WIDTH_BUFFER;
  }

  void setPreferredWidthBuffer( int preferredWidthBuffer ) {
    this.preferredWidthBuffer = preferredWidthBuffer;
  }

  boolean hasPreferredWidthBuffer() {
    return preferredWidthBuffer != EMPTY_PREFERED_WIDTH_BUFFER;
  }

  int getPreferredWidthBuffer() {
    return preferredWidthBuffer;
  }

  int getInnerHeight() {
    int innerHeight = getItemCount() * parent.getItemHeight();
    for( int i = 0; i < getItemCount(); i++ ) {
      TreeItem item = getItem( i );
      if( item.getExpanded() ) {
        innerHeight += item.getInnerHeight();
      }
    }
    return innerHeight;
  }

  private void materialize() {
    if( !isCached() ) {
      parent.checkData( this, this.index );
    }
  }

  void markCached() {
    if( parent.isVirtual() ) {
      cached = true;
    }
  }

  private void clearCached() {
    if( parent.isVirtual() ) {
      cached = false;
    }
  }

  boolean isCached() {
    boolean result = true;
    if( parent.isVirtual() ) {
      result = cached;
    }
    return result;
  }

}
