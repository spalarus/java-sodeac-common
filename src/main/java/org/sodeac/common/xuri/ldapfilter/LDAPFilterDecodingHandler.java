/*******************************************************************************
 * Copyright (c) 2016, 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.xuri.ldapfilter;

import java.io.Serializable;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;
import org.sodeac.common.xuri.ComponentType;
import org.sodeac.common.xuri.ExtensionHandleObject;
import org.sodeac.common.xuri.FormatException;
import org.sodeac.common.xuri.IDecodingExtensionHandler;

/**
 * XURI decoding extension handler to decode ldap filter items of type {@link IFilterItem}
 * 
 * @author Sebastian Palarus
 * @since 1.0
 * @version 1.0
 *
 */
@Component(service=IDecodingExtensionHandler.class,immediate=true)
public class LDAPFilterDecodingHandler implements IDecodingExtensionHandler<IFilterItem>, Serializable
{
	//TODO  implement escaping mechanism from  https://tools.ietf.org/search/rfc4515
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9187580171255086052L;
	
	private transient static volatile LDAPFilterDecodingHandler INSTANCE = null;
	
	public static final char OPENER = IFilterItem.OPENER;
	public static final char CLOSER = IFilterItem.CLOSER;
	public static final char ESCAPE = IFilterItem.ESCAPE;
	public static final char[] OPENER_CHARACTERS = new char[] {OPENER};
	public static final char[] CLOSER_CHARACTERS = new char[] {CLOSER};
	
	
	public static LDAPFilterDecodingHandler getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new LDAPFilterDecodingHandler();
		}
		return INSTANCE;
	}
	
	@Override
	public String getType()
	{
		return LDAPFilterExtension.TYPE;
	}
	
	@Override
	public ComponentType[] getApplicableComponents()
	{
		return new ComponentType[] {ComponentType.AUTHORITY,ComponentType.PATH,ComponentType.QUERY,ComponentType.FRAGMENT};
	}

	@Override
	public int parseRawExtensionString(ExtensionHandleObject extensionHandleObject)
	{
		char c;
		int openerCount = 0;
		boolean inEscape = false;
		
		for(; extensionHandleObject.position < extensionHandleObject.fullPath.length(); extensionHandleObject.position++)
		{
			c = extensionHandleObject.fullPath.charAt(extensionHandleObject.position);
			
			if(inEscape)
			{
				inEscape = false;
				extensionHandleObject.rawResult.append(c);
				continue;
			}
			
			if(c == ESCAPE)
			{
				inEscape = true;
				extensionHandleObject.rawResult.append(c);
				continue;
			}
			
			if(c == OPENER)
			{
				openerCount++;
			}
			
			if(c == CLOSER)
			{
				if(openerCount == 0)
				{
					String expression = extensionHandleObject.rawResult.toString();
					if(expression.trim().startsWith("(") && expression.trim().endsWith(")"))
					{
						extensionHandleObject.extension = new LDAPFilterExtension(expression);
					}
					else
					{
						extensionHandleObject.extension = new LDAPFilterExtension("(" + expression + ")");
					}
					return extensionHandleObject.position + 1;
				}
				else
				{
					openerCount--;
				}
			}
			
			extensionHandleObject.rawResult.append(c);
		}
		
		throw new FormatException("no closing sequence \"" + new String(getCloserCharacters(extensionHandleObject.component)) + "\" found in " + getType() + " : " + extensionHandleObject.rawResult.toString());
	}
	
	

	@Override
	public int openerCharactersMatched(ExtensionHandleObject extensionHandleObject)
	{
		return extensionHandleObject.fullPath.charAt(extensionHandleObject.position) == OPENER ?  extensionHandleObject.position + 1 : -1;
	}
	
	@Override
	public IFilterItem decodeFromString(String raw)
	{
		IFilterItem rootFilter = null;
		IFilterItem currentFilter = null;
		IFilterItem previewsFilter = null;
		LinkedList<IFilterItem> filterItemPath = new LinkedList<IFilterItem>(); 
		LinkedList<Integer> unclosedChildOpenerPath = new LinkedList<Integer>();
		
		StringBuilder sb = new StringBuilder();
		boolean openMode = true;
		//boolean inLinkerMode = false;
		boolean inAttributeMode = false;
		
		boolean invert = false;
		
		boolean inAttributeNameMode = false;
		boolean inAttributeValueMode = false;
		
		int unclosedOpener = 0;
		int unclosedChildOpener = 0;
		
		char c;
		rawloop:
		for(int i = 0; i < raw.length(); i++)
		{
			c = raw.charAt(i);
			
			switch (c) 
			{
				case IFilterItem.OPENER:
					if(inAttributeMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / (attribute mode)");
					}
					unclosedOpener++;
					unclosedChildOpener++;
					openMode = true;
					break;
				case IFilterItem.CLOSER:
					unclosedOpener--;
					unclosedChildOpener--;
					
					if(unclosedOpener < 0)
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / too much closer");
					}
					
					if(unclosedChildOpener < 0)
					{
						if(inAttributeMode)
						{
							if(! inAttributeValueMode)
							{
								throw new FormatException("unexpected position for " + c + " : " + i + " / no operator");
							}
							((Attribute)currentFilter).setValue(sb.toString());
						}
						filterItemPath.removeLast();
						currentFilter = null;
						if(! filterItemPath.isEmpty())
						{
							currentFilter = filterItemPath.getLast();
						}
						if(currentFilter == null)
						{
							if(! filterItemPath.isEmpty())
							{
								throw new FormatException("unexpected position for " + c + " : " + i + " / filterItemPath is not empty");
							}
							break rawloop;
						}
						unclosedChildOpener = unclosedChildOpenerPath.removeLast() - 1;
					}
					
					if(currentFilter instanceof Attribute)
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / in attribute");
					}
					
					openMode = false;
					inAttributeNameMode = false;
					inAttributeValueMode = false;
					// inLinkerMode = currentFilter instanceof AttributeLinker;
					inAttributeMode = false;
					
					break;
				case ' ':
					if(inAttributeValueMode)
					{
						sb.append(c);
					}
					break;
				case '\t':
					if(inAttributeValueMode)
					{
						sb.append(c);
					}
					break;
				case IFilterItem.LESS_STARTSEQ:
					
					if(! inAttributeMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / not in attribute mode");
					}
					
					if(inAttributeNameMode)
					{
						if(raw.charAt(i+1) == IFilterItem.EQUAL)
						{
							if(sb.toString().isEmpty())
							{
								throw new FormatException("unexpected position for " + c + " : " + i + " / attribute name is empty");
							}
							((Attribute)currentFilter).setName(sb.toString());
							((Attribute)currentFilter).setOperator(ComparativeOperator.LESS);
							sb.setLength(0);
							inAttributeNameMode = false;
							inAttributeValueMode = true;
							i++;
						}
						else
						{
							throw new FormatException("unexpected position for " + c + " : " + (i+1) + " / expect '=' ");
						}
					}
					else if(inAttributeValueMode)
					{
						sb.append(c);
					}
					else
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / in mad attribute mode ");
					}
				
					break;
				case IFilterItem.GREATER_STARTSEQ:
					
					if(! inAttributeMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / not in attribute mode");
					}
					
					if(inAttributeNameMode)
					{
						if(raw.charAt(i+1) == IFilterItem.EQUAL)
						{
							if(sb.toString().isEmpty())
							{
								throw new FormatException("unexpected position for " + c + " : " + i + " / attribute name is empty");
							}
							((Attribute)currentFilter).setName(sb.toString());
							((Attribute)currentFilter).setOperator(ComparativeOperator.GREATER);
							sb.setLength(0);
							inAttributeNameMode = false;
							inAttributeValueMode = true;
							i++;
						}
						else
						{
							throw new FormatException("unexpected position for " + c + " : " + (i+1) + " / expect '=' ");
						}
					}
					else if(inAttributeValueMode)
					{
						sb.append(c);
					}
					else
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / in mad attribute mode ");
					}
				
					break;
				case IFilterItem.APPROX_STARTSEQ:
					
					if(! inAttributeMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / not in attribute mode");
					}
					
					if(inAttributeNameMode)
					{
						if(raw.charAt(i+1) == IFilterItem.EQUAL)
						{
							if(sb.toString().isEmpty())
							{
								throw new FormatException("unexpected position for " + c + " : " + i + " / attribute name is empty");
							}
							((Attribute)currentFilter).setName(sb.toString());
							((Attribute)currentFilter).setOperator(ComparativeOperator.APPROX);
							sb.setLength(0);
							inAttributeNameMode = false;
							inAttributeValueMode = true;
							i++;
						}
						else
						{
							throw new FormatException("unexpected position for " + c + " : " + (i+1) + " / expect '=' ");
						}
					}
					else if(inAttributeValueMode)
					{
						sb.append(c);
					}
					else
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / in mad attribute mode ");
					}
				
					break;
				case IFilterItem.EQUAL:
					
					if(! inAttributeMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / not in attribute mode");
					}
					
					if(inAttributeNameMode)
					{
						if(sb.toString().isEmpty())
						{
							throw new FormatException("unexpected position for " + c + " : " + i + " / attribute name is empty");
						}
						((Attribute)currentFilter).setName(sb.toString());
						((Attribute)currentFilter).setOperator(ComparativeOperator.EQUAL);
						sb.setLength(0);
						inAttributeNameMode = false;
						inAttributeValueMode = true;
					}
					else if(inAttributeValueMode)
					{
						sb.append(c);
					}
					else
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " / in mad attribute mode ");
					}
				
					break;
				case IFilterItem.NOT:
					
					if(! openMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i);
					}
					
					invert = !invert;
					break;
				case IFilterItem.AND:
					
					if(! openMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i);
					}
					
					// add filter
					previewsFilter = currentFilter;
					currentFilter = new AttributeLinker();
					currentFilter.setInvert(invert);
					((AttributeLinker)currentFilter).setOperator(LogicalOperator.AND);
					if(previewsFilter != null)
					{
						((AttributeLinker)previewsFilter).addItem(currentFilter);
					}
					filterItemPath.addLast(currentFilter);
					unclosedChildOpenerPath.addLast(unclosedChildOpener);
					unclosedChildOpener = 0;
					
					// (re)set modes
					// inLinkerMode = true;
					inAttributeMode = false;
					inAttributeNameMode = false;
					inAttributeValueMode = false;
					openMode = false;
					invert = false;
					
					// set as root if first
					if(rootFilter == null)
					{
						rootFilter = currentFilter;
					}
					break;
				case IFilterItem.OR:
					
					if(! openMode)
					{
						throw new FormatException("unexpected position for " + c + " : " + i);
					}
					
					// add filter
					previewsFilter = currentFilter;
					currentFilter = new AttributeLinker();
					currentFilter.setInvert(invert);
					((AttributeLinker)currentFilter).setOperator(LogicalOperator.OR);
					if(previewsFilter != null)
					{
						((AttributeLinker)previewsFilter).addItem(currentFilter);
					}
					filterItemPath.addLast(currentFilter);
					unclosedChildOpenerPath.addLast(unclosedChildOpener);
					unclosedChildOpener = 0;
					
					// (re)set modes
					// inLinkerMode = true;
					inAttributeMode = false;
					inAttributeNameMode = false;
					inAttributeValueMode = false;
					openMode = false;
					invert = false;
					
					// set as root if first
					if(rootFilter == null)
					{
						rootFilter = currentFilter;
					}
					break;
				default:
					if(openMode)
					{
						previewsFilter = currentFilter;
						currentFilter = new Attribute();
						currentFilter.setInvert(invert);
						filterItemPath.addLast(currentFilter);
						unclosedChildOpenerPath.addLast(unclosedChildOpener);
						unclosedChildOpener = 0;
						
						if(previewsFilter != null)
						{
							if(! (previewsFilter instanceof AttributeLinker))
							{
								throw new FormatException("parent of attribute must be a linker! pos: " + i);
							}
							if(previewsFilter != null)
							{
								((AttributeLinker)previewsFilter).addItem(currentFilter);
							}
						}
						
						// (re)set modes
						// inLinkerMode = false;
						inAttributeMode = true;
						inAttributeNameMode = true;
						inAttributeValueMode = false;
						openMode = false;
						invert = false;
						
						// append char
						sb.setLength(0);
						sb.append(c);
						
						// set as root if first
						if(rootFilter == null)
						{
							rootFilter = currentFilter;
						}
					}
					else if(inAttributeMode)
					{
						// append char
						sb.append(c);
					}
					else
					{
						throw new FormatException("unexpected position for " + c + " : " + i + " => not in linkermode");
					}
					break;
			}
		}
		
		return rootFilter;
	}
	
	/**
	 * getter for opener characters
	 * 
	 * @param component applicable component
	 * @return opener characters
	 */
	public char[] getOpenerCharacters(ComponentType component)
	{
		return OPENER_CHARACTERS;
	}

	/**
	 * setter for closer characters
	 * 
	 * @param component applicable component
	 * @return closer characters
	 */
	public char[] getCloserCharacters(ComponentType component)
	{
		return CLOSER_CHARACTERS;
	}
}
