//
// FauxjoImpl
//
// Copyright (C) 2007 Brian Stevens.
//
//  This file is part of the Fauxjo Library.
//
//  The Fauxjo Library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  The Fauxjo Library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with the Fauxjo Library; if not, write to the Free
//  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
//  02111-1307 USA.
//

package net.fauxjo;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Convenient implementation of Fauxjo bean.
 */
public abstract class FauxjoImpl implements Fauxjo
{
    // ============================================================
    // Fields
    // ============================================================

    private Schema _schema;

    // ============================================================
    // Methods
    // ============================================================

    // ----------
    // public
    // ----------

    /**
     * This should return a single object that is unique for this object.
     */
    public Object getPrimaryKey()
    {
        try
        {
            BeanInfo info = Introspector.getBeanInfo( getClass() );
            ArrayList<Object> values = new ArrayList<Object>();
            for ( PropertyDescriptor prop : info.getPropertyDescriptors() )
            {
                Method readMethod = prop.getReadMethod();
                if ( readMethod == null )
                {
                    continue;
                }
                else if ( !readMethod.isAnnotationPresent( FauxjoPrimaryKey.class ) )
                {
                    continue;
                }

                Object value = readMethod.invoke( this, new Object[]
                {
                } );
                values.add( value );
            }

            if ( values.size() == 0 )
            {
                return null;
            }
            else if ( values.size() == 1 )
            {
                return values.get( 0 );
            }

            // Convert all the objects to a single concatenated String
            StringBuilder builder = new StringBuilder();
            for ( Object value : values )
            {
                builder.append( value );
                builder.append( "~" );
            }
            return builder.toString();
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );
        }
    }

    public Schema getSchema()
    {
        return _schema;
    }

    public void setSchema( Schema schema )
    {
        _schema = schema;
    }

    /**
     * The schema is passed in because there is no guarantee that the schema was set on the
     * fauxjo object.
     */
    public boolean isInDatabase( Schema schema )
        throws FauxjoException
    {    	
        try
        {
        	if ( !isNew( schema ) )
	        {
	        	return false;
	        }
        
            Home<?> home = schema.getHome( getClass() );
            Method pkMethod = schema.findPrimaryFinder( home );
            Object[] pkValues = schema.findPrimaryKey( this );
            Object val = pkMethod.invoke( home, pkValues );

            if ( val == null )
            {
                return false;
            }

            return true;
        }
        catch ( Throwable ex )
        {
            throw new FauxjoException( ex );
        }
    }

    /**
     * The schema is passed in because there is no guarantee that the schema was set on the
     * fauxjo object.
     */
    public boolean isNew( Schema schema )
        throws FauxjoException
    {
    	if ( schema == null )
    	{
    		return false;
    	}
    	
    	try
    	{
	    	Object[] pkValues = schema.findPrimaryKey( this );
	    	
	    	// if no values, not valid
	        if ( pkValues == null )
	        {
	            return false;
	        }
	        
	        // if any values null, not valid
	        for ( Object key : pkValues )
	        {
	        	if ( key == null )
	        	{
	        		return false;
	        	}
	        }
	        
	        return true;
    	} 
    	catch ( Throwable ex )
    	{
    		throw new FauxjoException( ex );
    	}
    }

    public int hashCode()
    {
        // Any nulled hash keys equate to default of zero.
        return getPrimaryKey() == null ? 0 : getPrimaryKey().hashCode();
    }

    public boolean equals( Object other )
    {
        if ( getPrimaryKey() != null && other != null && other.getClass().equals( getClass() ) )
        {
            return getPrimaryKey().equals( ( (FauxjoImpl)other ).getPrimaryKey() );
        }
        return super.equals( other );
    }
}

