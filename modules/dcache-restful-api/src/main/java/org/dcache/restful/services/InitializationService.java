package org.dcache.restful.services;

import javax.security.auth.Subject;
import javax.servlet.ServletContext;

import java.security.AccessController;

import diskCacheV111.util.PnfsHandler;

import org.dcache.auth.attributes.Restriction;
import org.dcache.auth.attributes.Restrictions;
import org.dcache.cells.CellStub;
import org.dcache.util.list.ListDirectoryHandler;

/**
 *
 */
public class InitializationService
{
    public final static String DL = "org.dcache.restful";
    public final static String CS = "org.dcache.restful.CS";

    public static Subject getSubject()
    {
        return Subject.getSubject(AccessController.getContext());
    }

    public static Restriction getRestriction()
    {
        return Restrictions.readOnly();
    }

    public static ListDirectoryHandler getListDirectoryHandler(ServletContext ctx)
    {
        return (ListDirectoryHandler) (ctx.getAttribute(DL));
    }

    public static PnfsHandler getPnfsHandler(ServletContext ctx)
    {
        CellStub cellStub = (CellStub) (ctx.getAttribute(CS));
        PnfsHandler handler = new PnfsHandler(cellStub);
        handler.setSubject(getSubject());

        return handler;
    }
}
