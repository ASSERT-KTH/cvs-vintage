/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.tigris.scarab.feeds;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.security.UserManager;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.services.security.ScarabSecurity;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Sample Servlet that serves a feed created with Rome.
 * <p>
 * The feed type is determined by the 'type' request parameter, if the parameter is missing it defaults
 * to the 'default.feed.type' servlet init parameter, if the init parameter is missing it defaults to 'atom_0.3'
 * <p>
 * @author Alejandro Abdelnur
 *
 */
public class FeedServlet extends HttpServlet {
    private static final String DEFAULT_FEED_TYPE = "default.feed.type";
    private static final String FEED_TYPE = "type";
    private static final String MIME_TYPE = "application/xml; charset=UTF-8";
    private static final String COULD_NOT_GENERATE_FEED_ERROR = "Could not generate feed";
    private static final String COULD_NOT_GENERATE_FEED_ERROR_DATABASE = "Could not retrive data successfully";



    private String _defaultFeedType;

    public void init() {
        _defaultFeedType = getServletConfig().getInitParameter(DEFAULT_FEED_TYPE);
        _defaultFeedType = (_defaultFeedType!=null) ? _defaultFeedType : "atom_0.3";
    }

    public void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException {
        try {
            Query query = QueryManager.getInstance(new Long(280));
            ScarabUser user1 = ScarabUserManager.getInstance(new NumberKey(1), false);
            QueryFeed queryFeed = new QueryFeed(query,user1);
            SyndFeed feed = queryFeed.getFeed();

            String feedType = req.getParameter(FEED_TYPE);
            feedType = (feedType!=null) ? feedType : _defaultFeedType;
            feed.setFeedType(feedType);

            res.setContentType(MIME_TYPE);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed,res.getWriter());
        }
        catch(TorqueException te){
            String msg = COULD_NOT_GENERATE_FEED_ERROR_DATABASE;
            log(msg,te);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,msg);
        }
        catch (FeedException ex) {
            String msg = COULD_NOT_GENERATE_FEED_ERROR;
            log(msg,ex);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,msg);
        }
        catch (Exception e) {
            String msg = COULD_NOT_GENERATE_FEED_ERROR;
            log(msg,e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,msg);
        }        
    }

   

}
