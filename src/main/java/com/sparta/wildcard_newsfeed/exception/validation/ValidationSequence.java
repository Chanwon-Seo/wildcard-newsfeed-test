package com.sparta.wildcard_newsfeed.exception.validation;

import jakarta.validation.GroupSequence;

import static com.sparta.wildcard_newsfeed.exception.validation.ValidationGroups.*;

@GroupSequence({NotBlankGroup.class, PatternGroup.class, SizeGroup.class, LengthGroup.class})
public interface ValidationSequence {
}