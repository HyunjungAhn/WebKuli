/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.lookup;

import java.util.HashMap;
import java.util.Map;

import fitlibrary.collection.array.ArrayParser;
import fitlibrary.collection.list.ListParser;
import fitlibrary.collection.map.MapParser;
import fitlibrary.collection.set.SetParser;
import fitlibrary.object.DomainObjectParser;
import fitlibrary.parser.ByStringParser;
import fitlibrary.parser.DelegateParser;
import fitlibrary.parser.DelegatingParser;
import fitlibrary.parser.FailingDelegateParser;
import fitlibrary.parser.Parser;
import fitlibrary.parser.graphic.GraphicParser;
import fitlibrary.parser.table.TableParser;
import fitlibrary.parser.tagged.TaggedStringParser;
import fitlibrary.parser.tree.TreeParser;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.Typed;

public class ParserSelector {
	// The following map only hold factories for things that can't change.
	// The map doesn't hold ParseDelegates because they may be specified afresh for each storytest
	private Map mapClassToParserFactory = new HashMap(10000,0.8F); // <Class,ParserFactory>
	
    public Parser parserFor(Evaluator evaluator, Typed typed, boolean isResult) {
    	Class classType = typed.asClass();
    	DelegateParser delegate = ParseDelegation.getDelegate(classType);
    	if (delegate != null)
    		return delegate.parser(evaluator, typed);
    	DomainObjectParser domainObjectParser = new DomainObjectParser(evaluator,typed);
    	if (domainObjectParser.hasFinder())
    		return domainObjectParser;
		ParserFactory parserFactory = (ParserFactory) mapClassToParserFactory.get(classType);
    	if (parserFactory == null) {
    		parserFactory = parserFactory(typed, isResult);
    		if (parserFactory != null)
    			mapClassToParserFactory.put(classType, parserFactory);
    		else {
    			if (classType != String.class && classType != Object.class)
    				return domainObjectParser;
    			return new DelegatingParser(new FailingDelegateParser(typed.asClass()),evaluator,typed);
    		}
    	}
    	return parserFactory.parser(evaluator, typed);
    }
	private ParserFactory parserFactory(Typed typed, boolean isResult) {
		// The order of the applicability tests here is significant
    	Class classType = typed.asClass();
        if (Map.class.isAssignableFrom(classType))
        	return mapParserFactory();
        if (SetParser.applicableType(classType))
            return setParserFactory();
		if (ArrayParser.applicableType(classType))
			return ArrayParser.parserFactory();
        if (ListParser.applicableType(classType))
            return listParserFactory();
        if (TreeParser.applicableType(classType))
            return TreeParser.parserFactory();
        if (TableParser.applicableType(classType))
            return TableParser.parserFactory();
        if (GraphicParser.applicableType(classType))
            return GraphicParser.parserFactory();
        if (TaggedStringParser.applicableType(classType))
            return TaggedStringParser.parserFactory();
        
        ParserFactory factory = LookupPrimitiveParser.parserFactory(typed);
        if (factory != null)
        	return factory;
		factory = ParseDelegation.delegateClassFactory(typed);
		if (factory != null)
			return factory;
    	if (isExtraFactoryFor(typed))
    		return extraFactory();
		if (canTreatAsString(classType, typed.hasMethodOrField(), isResult))
			return ByStringParser.parserFactory();
		return null;
	}
	// Overidden
	protected ParserFactory listParserFactory() {
    	return ListParser.parserFactory();
	}
    // Overidden
    protected ParserFactory setParserFactory() {
    	return SetParser.parserFactory();
    }
    // Overidden
    protected ParserFactory mapParserFactory() {
    	return MapParser.parserFactory();
    }
    // Overidden
	protected boolean isExtraFactoryFor(Typed typed) {
		return false;
	}
    // Overidden
	protected ParserFactory extraFactory() {
		return null;
	}
    private boolean canTreatAsString(Class type, boolean hasCaller, boolean isResult) {
    	return isResult && hasCaller && type == Object.class;
    }
}
