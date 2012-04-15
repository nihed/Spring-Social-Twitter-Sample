/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.quickstart;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.social.twitter.api.CursoredList;


import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Simple little @Controller that invokes Facebook and renders the result. The
 * injected {@link Facebook} reference is configured with the required
 * authorization credentials for the current user behind the scenes.
 *
 * @author Keith Donald
 */
@Controller
public class HomeController {

    private final Twitter twitter;

    @Inject
    public HomeController(Twitter twitter) {
        this.twitter = twitter;

    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        final String screenName = twitter.userOperations().getScreenName();
        final TwitterProfile userProfile = twitter.userOperations().getUserProfile();
        final String name = userProfile.getName();
        final String profileImageUrl = userProfile.getProfileImageUrl();
        final int followersCount = userProfile.getFollowersCount();
        final int friendsCount = userProfile.getFriendsCount();
        final int listedCount = userProfile.getListedCount();
        final int statusesCount = userProfile.getStatusesCount();
        final Date createdDate = userProfile.getCreatedDate();
        final int days = (int) (((new Date()).getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24));
        String pattern = "dd MMM yyyy";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        model.addAttribute("screenName", screenName);
        model.addAttribute("name", name);
        model.addAttribute("profileImageUrl", profileImageUrl);
        model.addAttribute("followersCount", followersCount);
        model.addAttribute("friendsCount", friendsCount);
        model.addAttribute("listedCount", listedCount);
        model.addAttribute("statusesCount", statusesCount);
        model.addAttribute("createdDate", format.format(createdDate));
        model.addAttribute("days", days);

        return "home";
    }
    
    @RequestMapping(value ="/ErrorPage")
    public String error(Model model) {
        return "ErrorPage";
    }

    @RequestMapping(value = "/follower.csv", method = RequestMethod.GET)
    public void follower(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final CursoredList<TwitterProfile> followers = twitter.friendOperations().getFollowers();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=follower.csv");

        StringBuffer sb = new StringBuffer();
        sb.append("ScreenName");
        sb.append(';');
        sb.append("Name");
        sb.append(';');
        sb.append("CreatedDate");
        sb.append(';');
        sb.append("FollowersCount");
        sb.append(';');
        sb.append("Description");
        sb.append("\n");

        for (TwitterProfile twitterProfile : followers) {
            sb.append(twitterProfile.getScreenName().replace(";", "\\;"));
            sb.append(';');
            sb.append(twitterProfile.getName().replace(";", "\\;"));
            sb.append(';');
            sb.append(twitterProfile.getCreatedDate());
            sb.append(';');
            sb.append(twitterProfile.getFollowersCount());
            sb.append(';');
            final String description = twitterProfile.getDescription();
            if (description != null) {
                String replace = description.replace(";", "\\;");
                replace = description.replace("\r", " ");
                sb.append(replace.replace("\n", " "));
            }
            sb.append('\n');
        }


        PrintWriter pw = response.getWriter();
        pw.append(sb);
        pw.flush();
        pw.close();
        twitter.timelineOperations().updateStatus("Just downloaded my followers list from http://twitter-nihed.rhcloud.com/");
        

    }
}