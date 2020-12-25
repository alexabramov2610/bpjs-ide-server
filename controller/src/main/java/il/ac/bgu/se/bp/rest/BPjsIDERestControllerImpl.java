package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.DebugRequest;
import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.service.BPjsIDEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bpjs")
public class BPjsIDERestControllerImpl implements BPjsIDERestController {

    @Autowired
    private BPjsIDEService bPjsIDEService;

    private static final Logger logger = new Logger(BPjsIDERestControllerImpl.class);

    @Override
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public @ResponseBody
    ExecuteBPjsResponse run(@RequestBody DebugRequest code) {
        return bPjsIDEService.run(code);
    }

    @Override
    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    public @ResponseBody
    ExecuteBPjsResponse debug(@RequestBody DebugRequest code) {
        return bPjsIDEService.debug(code);
    }

}
