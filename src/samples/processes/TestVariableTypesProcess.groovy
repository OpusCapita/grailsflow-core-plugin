import com.jcatalog.grailsflow.utils.ConstantUtils 
import com.jcatalog.grailsflow.process.Link 
import com.jcatalog.grailsflow.process.Document 
import Address 


/** 
 * Please remember: if you want to use variable value in any expression - use # symbol. 
 * Example: 'Value is #SomeProcessVariable' 
 */ 
class TestVariableTypesProcess {
    public String string_var  = new String("simple string") 
    public String textarea_var  = new String("textarea \nmultiline \nstring") 
    public String selectbox_var  = new String("two") 
    public Date date_var  = new Date(1251320400000) 
    public Long long_var  = new Long("123456") 
    public Integer integer_var  = new Integer("123") 
    public Double double_var  = new Double("12.3") 
    public Boolean boolean_var  = new Boolean('false') 
    public Link link_var  = new Link(path: 'www.google.com', description: 'Google') 
    public Document document_var  
    public Address object_list  
    public Address object_search  
    public String string_noview  = new String("default view string")

    public List<String> stringItems = ["cat", "dog", "elefant"]
    public List<Integer> intItems = [13, 203, 666]
    public List<Double> doubleItems = [new Double(13.67), 203.22, 666.8]
    public List<Link> linkItems = [new Link(path: 'http://groovy.codehaus.org/', description: 'Groovy'), new Link(path: 'http://grails.org/', description: 'Grails') ]
    public List<Long> longItems = [123456, 789104]
    public List<Date> dateItems = [new Date(1251320400000), new Date(12513267400460)]
    public List<Boolean> booleanItems = [true, false, true, true]

    def descriptions = { 
        TestVariableTypes( description_de : "Process demonstrates support of variables types and views",
          description_en : "Process demonstrates support of variables types and views" ) 
        editValues( label_en : "Edit values" ) 
        viewValues( label_en : "View values" ) 
    }


    def views = { 
        string_var( simpleView( styleClass: "required", size: 15 ) ) 
        textarea_var( textAreaView( rows: 5, cols: 15 ) ) 
        selectbox_var( selectBoxView( items: ["one", "two", "tree"] ) ) 
        date_var( dateView(  ) ) 
        long_var( simpleView(  ) ) 
        integer_var( simpleView( size: 5 ) ) 
        double_var( simpleView( size: 5 ) ) 
        boolean_var( checkBoxView(  ) ) 
        link_var( linkView(  ) ) 
        document_var( documentView( size: 50 ) ) 
        object_list( listObjectsView( displayKey: "addressID" ) ) 
        object_search( externalSearchObjectView( displayKey: "addressID", searchUrl: "search/address", additionalFields: "name1, city" ) )
        string_noview(  )
        dateItems(itemsView())
        linkItems(itemsView(styleClass: 'links'))
        stringItems(itemsView(styleClass: 'strings'))
        longItems(itemsView(styleClass: 'longs'))
    }


    def constraints = {
        document_var(required: true)
    }


    def TestVariableTypesProcess = { 
      start(isStart: true) { 
        action { 
 
          return "okay"
        } 
        on("okay").to([ "editValues" ]) 
      } 

      editValuesWait(dueDate: 60000, editorType: ConstantUtils.EDITOR_AUTO) {
        variable( link_var: ConstantUtils.WRITE_READ, double_var: ConstantUtils.WRITE_READ, selectbox_var: ConstantUtils.WRITE_READ, string_var: ConstantUtils.REQUIRED, date_var: ConstantUtils.WRITE_READ, object_list: ConstantUtils.WRITE_READ, string_noview: ConstantUtils.WRITE_READ, textarea_var: ConstantUtils.WRITE_READ, object_search: ConstantUtils.WRITE_READ, document_var: ConstantUtils.REQUIRED, boolean_var: ConstantUtils.WRITE_READ, long_var: ConstantUtils.WRITE_READ, integer_var: ConstantUtils.WRITE_READ, booleanItems: ConstantUtils.READ_ONLY, stringItems: ConstantUtils.WRITE_READ, linkItems: ConstantUtils.WRITE_READ, longItems: ConstantUtils.READ_ONLY, dateItems: ConstantUtils.WRITE_READ)
        action { 

        } 
        on("view").to([ "viewValues" ]) 
        on("finish").to([ "finish" ]) 
      } 

      viewValuesWait(dueDate: 60000, editorType: ConstantUtils.EDITOR_AUTO) {
        variable( object_list: ConstantUtils.READ_ONLY, link_var: ConstantUtils.READ_ONLY, textarea_var: ConstantUtils.READ_ONLY, double_var: ConstantUtils.READ_ONLY, boolean_var: ConstantUtils.READ_ONLY, string_noview: ConstantUtils.READ_ONLY, date_var: ConstantUtils.READ_ONLY, integer_var: ConstantUtils.READ_ONLY, document_var: ConstantUtils.READ_ONLY, long_var: ConstantUtils.READ_ONLY, selectbox_var: ConstantUtils.READ_ONLY, string_var: ConstantUtils.READ_ONLY, object_search: ConstantUtils.READ_ONLY, booleanItems: ConstantUtils.WRITE_READ, stringItems: ConstantUtils.WRITE_READ, linkItems: ConstantUtils.WRITE_READ, longItems: ConstantUtils.READ_ONLY)
        action { 

        } 
        on("edit").to([ "editValues" ]) 
        on("finish").to([ "finish" ]) 
      } 

      finish(isFinal: true) { 
        action { 

        } 
      } 

    } 
 }