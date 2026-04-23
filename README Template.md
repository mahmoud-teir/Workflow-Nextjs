---
description: 
---

1. A Unique Header ❄️
Your profile’s header is the first thing that people will observe so it must stand out from the other profiles. We want that initial “hook” that attracts the viewer. And for that to happen, my suggestion would be to avoid following the common design norms. For example, many developers use this layout for their “About” section

It’s completely fine to use this template though as long as you change other aspects of your profile. I went in another direction for creating the header and then added the “About Me” section after that. I’ll guide you along the way with the resources I used for that.

The very first thing that you can see is that animated header with the text “Hey Everyone!” used GitHub repo . came across this great resource while searching for ways to decorate  GitHub repo. You can add background images and text on top of them and also, who doesn’t love animations! It’s super simple to use and has been well documented on the Repo. Here is my configuration of the render.

After inserting a simple heading, to provide links to my various accounts like LinkedIn, Medium, Dev.to, and also my portfolio website, I wanted a minimalistic and textless way to do it. So, I decided to use icons. There are many online tools available that provide thousands of free icons to use. I used IconFinder and personally loved it. There are many other popular options available that you can use like Shields.io, markdown-badges, vector-logo-zone, simple-icons, etc.

Now comes the fun part, adding that glorious GIF! GIFS make our profile more dynamic and eye-catching. Honestly, you can put up any GIF you want. It can be a popular meme, a programming gif, an iconic scene from a movie or a tv show, or something that tells people a little bit about your hobbies. In my case, it’s anime so that’s what I went with. Popular gif sharing websites like Giphy and Tenor can be used to pull any gif you like and it works the same way as adding the icons, just copy the image address and paste it inside the “src” attribute of the <img> tag.

2. The “About Me” Section 👨‍💻
This is the section that I was talking about earlier where most developers use the template shown above. If you want to make your profile stand out, then I would suggest changing the design aspects of this section as well. I went ahead and used YAML format while editing the readme so that the information reads like code when you preview the profile.

It adds a touch of professionalism and also as a bonus, looks neat! To display this format, just wrap your text as shown below and you’ll be good to go:

3. Tools and Tech Stuff 🧰
In this section, you can showcase your skills and list the tools and technologies that you’re familiar with. I always prefer minimal and crisp design choices over cluttered data so I went ahead with icons this time as well. Us humans prefer information through visual mediums much more than anything else, right?

You can use all of the stuff that I mentioned above in step 2 like IconFinder, Shields.io, markdown-badges, vector-logo-zone, simple-icons, etc. But for this section, I would personally recommend DevIcon. Unlike other resources, DevIcon is built for providing the icons solely related to programming languages and development tools which makes it a perfect fit.

4. Your GitHub History 📈
Finally, at the end of your Profile README, you can practically include anything. Some developers put up what’s currently playing on their Spotify profile, some add their GitHub stats or some add a fun little snake game on your GitHub contribution graph like me which I’ll show you guys how to put up!

I begin with two GitHub ReadMe Stat Cards. One shows my total number of stars, commits and pull requests, etc. And the other one displays my most used Programming languages in percentages. You guys can get these cards from the popular GitHub ReadMe Stats Repo and the best part about these is that they are fully customizable with different settings and themes!

Next up is probably my favorite thing out of all of my profile elements. Making a Snake Game out of your GitHub Contribution Graph. It’s fairly easy to set up and looks extremely satisfying when the snake gobbles up your commit graph.

To set it up for your profile, we are going to use something called GitHub Actions. GitHub Actions are CI/CD tools in GitHub where you can initiate workflows that automatically run, deploy and build your stuff.