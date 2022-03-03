<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>TD Selection From TIP ${tip.name}</title>
</head>
<body>

    <h1>Trustmark Assessment Strategy</h1>
    <div class="pageSubsection">
        On this page, you must select those trustmark definitions you wish to assess from <b>${tip.name}</b>, version ${tip.tipVersion}.
    </div>

    <div class="pageContent">

        <g:if test="${flash.message}">
            <div class="alert alert-success" style="margin-bottom: 2em;">
                ${flash.message}
            </div>
        </g:if>

        <div style="">
            <a href="javascript:selectAll();" class="btn btn-default">Select All</a>
            <a href="javascript:selectNone();" class="btn btn-default">Select None</a>
            <a href="javascript:collapseAll();" class="btn btn-default">Collapse All</a>
            <a href="javascript:expandAll();" class="btn btn-default">Expand All</a>
        </div>

        <g:form action="processTreeResolve" method="POST">
            <input type="hidden" name="TIP_TO_RESOLVE" value="${params['TIP_TO_RESOLVE']}" />

            <div style="font-size: 120%">
                <table id="tipTreeTable_${tip.id}" class="table table-condensed table-bordered">
                    <tmpl:recursivelyBuildTipHtml data="${treeData}" />
                </table>
            </div>

            <div>
                <a href="${createLink(controller: 'home', action: 'index')}" class="btn btn-danger">&laquo; Cancel</a>
                <input type="submit" value="Done &raquo;" class="btn btn-primary" />
                <span>You currently have <span id="tdsCheckedValue">?</span> trustmark definitions selected.</span>
            </div>

        </g:form>

        <g:if test="${tdsAndTips.getAllTipsNeedingResolutionExcept(tip.id).size() > 0}">
            <div style="margin-top: 2em">
                <h4>Other TIPs needing resolution:</h4>
                <ul>
                    <g:each in="${tdsAndTips.getAllTipsNeedingResolutionExcept(tip.id)}" var="curTip">
                        <li>
                            <a href="${createLink(action: 'resolveTdsFromTip', params: [TIP_TO_RESOLVE: curTip.databaseId])}">
                                ${curTip.name}, version ${curTip.version}
                            </a>
                        </li>
                    </g:each>
                </ul>
            </div>
        </g:if>

        %{--<g:if test="${!tdsAndTips.getAllTipsNeedingResolutionExcept(tip.id).isEmpty()}">--}%

        %{--</g:if>--}%

        <script type="text/javascript">
            var TDS_CHECKED_COUNT = 0;
            var ROW_INDENT = 10;
            var CONST_INDENT = 10;

            $(document).ready(function(){
                $("#tipTreeTable_${tip.id}").treetable({expandable: true, clickableNodeNames: true, initialState: 'expanded'});
                setTimeout('resetCheckboxCount()', 250);
                setToggles();
                openAll();
            });

            function toggleAll(id){
                $('.childOf'+id+" .tdCheckbox").trigger('click');
            }

            function resetCheckboxCount(){
                TDS_CHECKED_COUNT = $('.tdCheckbox:checked').length;
                setCheckboxCount();
            }

            function setCheckboxCount(){
                $('#tdsCheckedValue').html(TDS_CHECKED_COUNT);
            }

            function updateCheckboxCount(checkbox){
                if( $(checkbox).prop('checked') == true ) {
                    TDS_CHECKED_COUNT++;
                }else{
                    TDS_CHECKED_COUNT--;
                }
                $('#tdsCheckedValue').html(TDS_CHECKED_COUNT);
            }

            function selectAll() {
                $('.tdCheckbox').prop('checked', true);
                resetCheckboxCount();
            }

            function selectNone() {
                $('.tdCheckbox').prop('checked', false);
                TDS_CHECKED_COUNT = 0;
                setCheckboxCount();
            }

            function collapseAll(){
                $("#tipTreeTable_${tip.id}").treetable("collapseAll");
            }

            function expandAll(){
                $("#tipTreeTable_${tip.id}").treetable("expandAll");
            }

            function toggleTE(obj){
                obj.nextElementSibling.classList.toggle("trust-expression");
            }

            // set the tree nodes for opening and closing on a click
            function setToggles()  {
                document.querySelectorAll(".tipcaret").forEach( t =>  {
                    t.addEventListener("click", function() {
                        var showt = this.classList.toggle("tipcaret-down");

                        var parentClass = 'childOf'+this.parentElement.parentElement.id;
                        var row = this.parentElement.parentElement.nextElementSibling;
                        while (row != null && row.classList.contains(parentClass))  {
                            if(showt)  {
                                row.classList.remove("tr-hide");
                                row.classList.add("tr-show");
                                if (row.querySelector(".tipcaret"))  {
                                    row.querySelector(".tipcaret").classList.toggle("tipcaret-down");
                                }
                            }  else {
                                row.classList.remove("tr-show");
                                row.classList.add("tr-hide");
                                if (row.querySelector(".tipcaret-down"))  {
                                    row.querySelector(".tipcaret-down").classList.toggle("tipcaret");
                                }
                            }
                            row = row.nextElementSibling;
                        }
                    });
                });
            }

            // open all tr rows
            function openAll()  {
                document.querySelector("table").querySelectorAll("tr").forEach( c => {
                    if ( c.querySelector(".tipcaret"))  {
                        c.querySelector(".tipcaret").classList.toggle("tipcaret-down");
                    }
                    var treeDepth = c.classList.value.split(' ').length - 2;
                    if ( treeDepth > 0)  {
                        treeDepth *= ROW_INDENT;
                        treeDepth += CONST_INDENT;
                        c.querySelector('td').style.paddingLeft = treeDepth+"px";
                    }
                    c.classList.toggle("tr-show");
                });
            }

        </script>

    </div>

</body>
</html>
