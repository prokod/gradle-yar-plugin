package com.github.prokod.gradle.yar

import org.gradle.api.GradleException

import java.util.regex.Pattern

class TaggingFormat {
    static final def TOKEN_BRANCH_NAME = "b"
    static final def TOKEN_PROJECT_VERSION = "v"
    final String format

    public TaggingFormat(String format) {
        this.format = format
    }

    def String generateTag(String b, String v) {
        def tag = format
        def tokenPattern = Pattern.compile('(#\\{([A-z]+)})')
        def m = tokenPattern.matcher(format)
        while (m.find()) {
            switch (m.group(2)) {
                case TOKEN_BRANCH_NAME:
                    tag = tag.replace(m.group(1), b)
                    break
                case TOKEN_PROJECT_VERSION:
                    tag = tag.replace(m.group(1), v)
                    break
                default:
                    throw new GradleException("Could not match against expression ${m.group(1)} with token ${m.group(2)}. Valid tokens are: ${TOKEN_BRANCH_NAME}, ${TOKEN_PROJECT_VERSION}")
            }
        }
        tag
    }

    def Pattern getTagPattern() {
        def tag = format
        def tokenPattern = Pattern.compile('(#\\{([A-z]+)})')
        def m = tokenPattern.matcher(format)
        while (m.find()) {
            switch (m.group(2)) {
                case TOKEN_BRANCH_NAME:
                    tag = tag.replace(m.group(1), '(\\S+)')
                    break
                case TOKEN_PROJECT_VERSION:
                    tag = tag.replace(m.group(1), '(\\d+)')
                    break
                default:
                    throw new GradleException("Could not match against expression ${m.group(1)} with token ${m.group(2)}. Valid tokens are: ${TOKEN_BRANCH_NAME}, ${TOKEN_PROJECT_VERSION}")
            }
        }
        Pattern.compile(tag)
    }
}
