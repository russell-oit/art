<%@ include file ="headerAdmin.jsp" %>

<%
//clear all mondrian caches
java.util.Iterator<mondrian.rolap.RolapSchema> schemaIterator =  mondrian.rolap.RolapSchema.getRolapSchemas();
while(schemaIterator.hasNext()){
    mondrian.rolap.RolapSchema schema = schemaIterator.next();
    mondrian.olap.CacheControl cacheControl = schema.getInternalConnection().getCacheControl(null);
        
    cacheControl.flushSchemaCache();  
}  
%>

 <div style="text-align:center">
	<p>Mondrian cache cleared</p>
</div>

<%@ include file ="/user/footer.jsp" %>


