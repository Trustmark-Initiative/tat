
<div class="pageContent">
    <form class="form-horizontal" role="form">
        <div class="form-group">
            <label for="inputStatus" class="col-sm-2 control-label">Status</label>
            <div class="col-sm-10">
                <ul class="nav nav-pills" id="inputStatus">
                    <li id="inputStatusUnknown"><a href="javascript:changeResultStatus('Not_Known')">Unknown</a></li>
                    <li id="inputStatusSatisfied"><a href="javascript:changeResultStatus('Satisfied')">Satisfied</a></li>
                    <li id="inputStatusNotSatisfied"><a href="javascript:changeResultStatus('Not_Satisfied')">Not Satisfied</a></li>
                    <li id="inputStatusNotApplicable"><a href="javascript:changeResultStatus('Not_Applicable')">N/A</a></li>
                </ul>
            </div>
        </div>

        <div class="form-group">
            <label for="inputComment" class="col-sm-2 control-label">
                <span id="inputCommentFeedback"></span> Comments
            </label>
            <div class="col-sm-10">
                <textarea class="form-control" id="inputComment" rows="5"></textarea>
            </div>
        </div>

        <div class="form-group">
            <label for="artifacts" class="col-sm-2 control-label">
                Artifacts
            </label>
            <div class="col-sm-10" id="artifacts">
                <div class="form-control-static">
                    <div id="assStepArtifacts">
                        TODO: Put in some global artifact controls.
                    </div><div>
                    <a href="#" class="btn btn-default btn-xs">Link</a>
                    <a href="#" class="btn btn-default btn-xs">Upload</a>
                </div>
                </div>
            </div>
        </div>

    </form>
</div>

<hr/>

<div id="assStepReqArtifactsContainer">
    &nbsp;
</div>
